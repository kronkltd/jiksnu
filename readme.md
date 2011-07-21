# Jiksnu ("zheek snoo")

Jiksnu is a Lojban compound word (lujvo) for the words "Jikca
Casnu". This translates roughly to "is a social type of interaction"

## About Jiksnu

Jiksnu is a social network in a box.

Jiksnu is built on top of the Ciste framework, and makes extensive use
of it's MVC framework and view and routing helpers.

## Security Warnings

This is project is still very much alpha-level software. I would not
recommend storing any important data until a full security audit can
be performed.

For example, the passwords are still being stored in plain text and
authorization is not checked for the various actions.

## License

I am reluctantly releasing this under the GPL as I'm using Tigase as
the XMPP server component.

## Technologies Used

For the HTTP component, I am using Aleph. It's written in Clojure 1.3,
built with Maven, uses MongoDB and Redis as the datastores, and Abdera for Atom
generation and parsing.

All code was written in Emacs.

## Donate

If you would like to help support this project, send your bitcoin to
1PKVaib8p7ypnmuovtRE8YN3yPkkY3JjPa
