Jiksnu is a social network in a box.

This is very much alpha at this point. I am only putting this out
there because it was my New Years resolution to release come code.

Jiksnu is a Lojban word meaning roughly "Social Interaction"

I am reluctantly releasing this under the GPL as I'm using Tigase as
the XMPP server component.

For the HTTP component, I am using Jetty. It's written in Clojure 1.3,
built with Maven, uses MongoDB as the datastore, and Abdera for Atom
generation and parsing.

All of the tests are written in Lazytest.

The web component is using Compojure, but on top of that I'm using my
render-ring framework to build a MVC framework.

I have built a Ring-like library for XMPP dispatch. This will convert
received packets into Clojure hash maps. These hash-maps will be
matched to produce an action and format pair which is then used by
render-ring.

This application started life as a project to produce a Tigase
plugin. Since then it was expanded so that the application embeds the
Tigase server and the Jetty server as well.

While much of the XMPP framework was working at one time, there have
been substantial refactoring since then, and the XMPP component is not
guarenteed to work at this time.

At this point, there is no security whatsoever. There is a mechanism
in the HTTP interface to supply a username, but no password. None of
the actions restrict if you are logged in or not.
