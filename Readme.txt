#Storlet Runtime Environment based on Kompics Framework

Main component contains a Jetty Web server component and a SRE component,
Jetty Web server component accept RESTful request from Notification Service and Object Service.
The current valid request types are Asynchronous Trigger, Synchronous Trigger and Delete (refer to VISION wiki WP 2.4), the web server then wraps the requests into different events and passes them to SRE component.
SRE component contains three different event handlers for each kind of request event.

Asynchronous Trigger event handler first checks whether the storlet is already loaded in the SRE, if not, then retrieve it from Object Service, unpack to a temporary dir and load the storlet class dynamically. Otherwise, if the storlet is existing in the SRE, then load it directly. Finally trigger it with the "EventModel" get from the request event.

Synchronous Trigger handling is very much similar to the Asynchonous ones, only the request format/structure is different. And the triggering process is blocked.

Delete event remove the loaded storlet from the HashMap and the relevant temporary directory.

Dynamic Loading a Storlet as a new component. Since storlets are not implemented as Kompics components, a storletwrapper is implemented to hold those storlets as components.
