# Hoot-Android

A powerful, flexible, lightweight Android library for making network requests and working with RESTful web APIs.

Hoot provides an easy way to make GET, PUT, POST and DELETE requests to a web server. All requests are performed off the main UI thread as AsyncTasks. Simply bind a listener to 
the request and you will be notified when it completes (successfully or not). You can perform as many synchronous requests as Android allows and get notified as each completes. It 
allows for an easy way to handle the reconnection of requests (for example on device rotations). Hoot supports basic authorization and helps to deserialize responses
to local data objects. It also uses the best transport mechanism available depending on the version of Android running.

## Example

Make a new HootRequest to the URL you want to request, bind a listener, and call execute on the method you want to use. That's it.

    HootRequest request = Hoot.createInstanceWithBaseUrl("https://www.twotoasters.com/someplace.json").createRequest();
    	
    request.bindListener(new HootRequestListener() {
	
		@Override
		public void onSuccess(HootRequest request, HootResult result) {		
		}
	
		@Override
		public void onRequestStarted(HootRequest request) {
		}
	
		@Override
		public void onRequestCompleted(HootRequest request) {
		}
	
		@Override
		public void onFailure(HootRequest request, HootResult result) {
		}
	
		@Override
		public void onCancelled(HootRequest request) {
		}
    });
		
    request.get().execute();

You can also set basic authorization (username/password), HTTP headers, post/get parameters, and additional http connection flags on your request.


## Building

You can build the library from the command-line using `ant`:

    # Package the application for distribution
    $ ant clean dist

    # Generate Javadoc for the project
    $ ant clean docs

> Note: In addition to building the project, the `dist` target will
> also invoke the `docs` target automatically.

## License

Licensed under the Apache License, Version 2.0 (see LICENSE).

