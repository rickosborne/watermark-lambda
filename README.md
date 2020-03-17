# watermark-lambda

A very simple proof-of-concept Lambda for adding watermarks to images.
See [`WatermarkPostHandler`](./src/main/java/org/rickosborne/watermark/lambda/WatermarkPostHandler.java) and [`WatermarkS3EventHandler`](./src/main/java/org/rickosborne/watermark/lambda/WatermarkS3EventHandler.java) for HTTP-POST and S3 Change Event handlers.

## What does it do?

The gist here is:

1. You configure a source bucket which will act as an inbox for your images you want to be watermarked.
1. You configure a watermark bucket and path to an image which will be used as your watermark.
   Generally, this will be a PNG.
   This _could_ be the same as the source bucket.
1. You configure a watermark position.
   This will most often be done in percentages, though it _could_ be done in absolute pixels.
1. You configure a destination bucket and path which will be used as an outbox for watermarked images.
   This _could_ be the same as the other buckets, but that's probably a bad idea.
1. You wire up the Lambda to receive S3 Create Events or HTTP POSTs.
1. Profit!

For S3 Create Event requests only the source bucket name and path can be extracted from the event.

For HTTP POST, see [`WatermarkPostRequest`](./src/main/java/org/rickosborne/watermark/lambda/WatermarkPostRequest.java) for request object details.
Only the `sourceKey` is required, presuming you have everything else set up via configuration.
(See [`WatermarkConfig`](./src/main/java/org/rickosborne/watermark/lambda/WatermarkConfig.java) for configuration property names.  Remember that `some.dotted.property` becomes `SOME_DOTTED_PROPERTY` if you want to use it as an environment variable.)
Alternatively, you can supply pretty much everything in the POST request, configuring nothing.

## What can I configure?

There are additional configuration options for:

* Whether you want to mark the destination image as Public.
* Whether you want to skip conversion if the destination exists (or overwrite it).
* Whether you want to rename the file with a key based on the source image.
  This takes a few extra cycles, but can help avoid accidental overwrites.
  It can also avoid processing the same image multiple times if it is uploaded with different names.
  The generated destination key is in the result.

*WARNING*: Be careful to *not* set up your destination the same as your source if you're using the S3 event!
You can end up in a very *expensive* infinite loop of events!

## How do I specify watermark position?

There are the basic position and size attributes: `left`, `right`, `top`, `bottom`, `width`, `height`.
Each has a matching `units` configuration which can be either `PERCENT` or `PIXELS`.
You likely *will not* specify all of those attributes!
Instead, you should mix and match to get the right effect.

For example, I like to have my watermark in the bottom right, and have it be ~10% of the width.
I can specify this with:

    right = 90%
    bottom = 90%
    width = 10%

Notice that I don't need to specify a `height` — it can be automatically calculated from the aspect ratio of the watermark.

You _could_ do something similar in absolute pixels if you wanted, but you're always measuring from the top left, so you'll want to be careful that you don't go off the edge:

    right = 1900 PIXELS
    bottom = 1000 PIXELS
    width = 10%

Yes, since each attribute has its own units you _could_ mix and match them.
No, there currently is no way to express that you want to measure from the right/bottom of the image.

