# REST autoproxy
Creates proxies to call remote REST services.
Simply create an interface and annotate it as you would a spring controller. Supported annotations are: RequestMapping, Request body, RequestParam, RequestHeader and PathVariable. Every method in the interface must be annotated with a RequestMapping annotation. To create the proxy, simply create a RestAutoproxyFactoryBean in your spring application context and it will create the implementation for you. The only thing needed is to provide the factory with the base url of your REST application.
The RequestMapping can be put on the type level of the interface to provide some sensible defaults (request methods, part of the URL etc...), however, a RequestMapping is still required on all the methods.
## Mapping response headers
It is currently possible to map all response headers into the resulting objects (using annotation MapResponseHeaders), however, either all of the response headers need to be mapped into the resulting object, or none of them. The final mapping is done using jackson, so you can ignore some response headers via jackson annotations (@JsonIgnoreProperties(ignoreUnknown = true)).
Annotation MapResponseHeaders can be placed on a method, or on the type level as a shortcut.
