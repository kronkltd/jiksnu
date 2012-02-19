Feature: Feed Subscription Admin
  In order to manage feed subscriptions
  As an administrator
  I want to be able to perform basic operations on subscriptions

Scenario: Index Feed Subscriptions, admin
  Given I am logged in as an admin
  When I go to the feed subscriptions admin index page
  Then I should see a list of feed subscriptions
