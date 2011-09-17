Feature: Subscriptions
  In order to allow subscriptions to be managed
  Should provide interactions on those subscriptions

Scenario: Admin page, unauthenticated
  Given the user is not logged in
  When I request the subscription index page
  Then the response is a redirect
  And I am redirected to the login page

Scenario: Admin page, user
  Given a normal user is logged in
  When I request the subscription index page
  Then the response is a redirect
  And I am redirected to the login page

Scenario: Admin page, user
  Given an admin is logged in
  When I request the subscription index page
  Then the response is sucsessful
  And I should see a subscription list
