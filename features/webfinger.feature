Feature: Webfinger
  In order to provide interop with other services
  Should implement the webfinger spec

Scenario: Fetching a Host Meta document
  Given the user is not logged in
  When I request the host-meta page
  Then the response is sucsessful
  And the content-type is "application/xrds+xml"
  And the host field matches the current domain

Scenario: Fetchin a User Meta document
  Given the user is not logged in
  And a user exists
  When I request the user-meta page for that user
  Then the response is sucsessful
  And the content-type is "application/xrds+xml"
  And the alias field matches that user's uri
