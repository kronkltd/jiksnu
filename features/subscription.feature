Feature: Subscriptions
  In order to allow subscriptions to be managed
  Should provide interactions on those subscriptions

Scenario: Admin page, unauthenticated
  Given the user is not logged in
  When I request the subscription index page
  Then I am redirected to the login page
  # And the response is a redirect

Scenario: Admin page, user
  Given a normal user is logged in
  When I request the subscription index page
  Then I am redirected to the login page
  # And the response is a redirect

Scenario: Admin page, user
  Given an admin is logged in
  When I request the subscription index page
  Then I should see a subscription list
  # And the response is sucsessful
