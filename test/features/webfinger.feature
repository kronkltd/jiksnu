Feature: Webfinger
  In order to provide interop with other services
  Should implement the webfinger spec

Scenario: Fetching a Host Meta document
  Given I am not logged in
  When I request the host-meta page with a client
  Then the host field matches the current domain
  And the content-type is "application/xrds+xml"
  And the response is sucsessful

Scenario: Fetchin a User Meta document
  Given I am not logged in
  And a user exists
  When I request the user-meta page for that user with a client
  Then the alias field matches that user's uri
  And the content-type is "application/xrds+xml"
  And the response is sucsessful
