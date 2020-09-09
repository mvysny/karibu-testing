Karibu-Testing doesn't officially support Spring. However, there are couple of very
simple example projects that might help you get started.

Please see [t-shirt shop example](https://github.com/mvysny/t-shirt-shop-example) on
an example on how to use Karibu-Testing with a Spring app.

Note that Spring Security is not supported: Spring Security uses Servlet Filter
which requires Servlet Container to be up and running, yet Karibu-Testing doesn't
start any Servlet Container. See [Issue #47](https://github.com/mvysny/karibu-testing/issues/47)
for more details.
