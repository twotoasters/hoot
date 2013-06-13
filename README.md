# Hoot-Android

A powerful, flexible, lightweight Android library for making network requests and working with RESTful web APIs.

Hoot provides an easy way to make GET, PUT, POST and DELETE requests to a web server. All requests are performed off the main UI thread as AsyncTasks. Simply bind a listener to 
the request and you will be notified when it completes (successfully or not). You can perform as many concurrent requests as Android allows and get notified as each completes. It 
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

## OAuth
Many popular APIs nowadays (Google, Facebook, Twitter etc.) use OAuth to perform user validation. <a href="https://github.com/twotoasters/AndrOAuth">AndrOAuth</a> is a simple library
that uses Hoot and makes it extremely easy to perform OAuth1.0 and 2.0 requests and validation.

## Building

You can build the library from the command-line using `ant`:

    # Package the application for distribution
    $ ant clean dist

    # Generate Javadoc for the project
    $ ant clean docs

> Note: In addition to building the project, the `dist` target will
> also invoke the `docs` target automatically.

## License

Copyright 2013, Two Toasters

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

	http://www.apache.org/licenses/LICENSE-2.0
	
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

