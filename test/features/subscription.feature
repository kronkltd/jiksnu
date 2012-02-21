Feature: Subscriptions
  In order to allow subscriptions to be managed
  Should provide interactions on those subscriptions

Scenario: Admin page, unauthenticated
  Given I am not logged in
  When I go to the subscription index page
  Then I should be at the login page
  # And the response is a redirect

Scenario: Admin page, user
  Given a normal user is logged in
  When I go to the subscription index page
  Then I should be at the login page
  # And the response is a redirect

Scenario: Admin page, user
  Given I am logged in as an admin
  When I go to the subscription index page
  Then I should see a subscription list
  # And the response is sucsessful

Scenario: Ostatus subscription page
  Given I am not logged in
  When I go to the ostatus sub page
  Then I should see a form

Scenario: User subscriptions, default
  Given I am not logged in
  And a user exists
  When I go to that user's subscriptions page
  Then I should get a "HTML" document

Scenario: User subscriptions, JSON
  Given I am not logged in
  And a user exists
  When I go to that user's subscriptions page with a "JSON" format
  Then I should get a "JSON" document
