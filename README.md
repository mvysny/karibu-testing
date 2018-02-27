[![Build Status](https://travis-ci.org/mvysny/karibu-testing.svg?branch=master)](https://travis-ci.org/mvysny/karibu-testing)

# Vaadin Serverless Testing

Vaadin-on-Kotlin promotes a testing technique called *serverless (browserless) testing*. With this approach, it's not the browser you issue
testing instructions to: we bypass the browser and the JavaScript-Server bridge, and talk directly to the server Vaadin components.

This approach has the following advantages:

* *Speed*. Server-side tests are typically 100x faster than Selenium and run in ~60 milliseconds.
* *Reliable*. We don't need arbitrary sleeps since we're server-side and we can hook into data fetching.
* Run headless since there's no browser.
* Can be run after every commit since they're fast.
* You don't even need to start the web server itself since we're bypassing the http parsing altogether!

A 15-minute [video](https://www.youtube.com/watch?v=XOhv3y2GXIE) explains everything behind the serverless testing technique.

## The Testing library is Standalone

The Serverless testing is an *approach* rather than a product, although it's backed by this support library.

You don't have to use Vaadin-on-Kotlin nor Karibu-DSL to use this approach; you don't even need to write your app in Kotlin.
You can just plug this library in into your Java+Vaadin-based project as a test dependency, and write only the test code in Kotlin.

## Getting started

* If you are using Vaadin 8, head to [Getting Started with Vaadin 8](karibu-testing-v8).
* If you are using Vaadin 10, head to [Getting Started with Vaadin 10](karibu-testing-v10).

## More Resources

* The [video](https://www.youtube.com/watch?v=XOhv3y2GXIE) which explains everything behind the serverless testing technique.
* The [Serverless web testing](http://mavi.logdown.com/posts/3147601) article describes this technique in more depth.

