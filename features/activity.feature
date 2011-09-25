Feature: Activities
  In order to have full control over activities
  As a user
  I want to be able to perform management tasks

Scenario: Viewing a public activity, unauthenticated
  Given I am not logged in
  And there is a public activity
  When I go to the page for that activity
  Then I should see that activity

Scenario: Viewing a private activity, unauthenticated
  Given I am not logged in
  And there is a private activity
  When I go to the page for that activity
  Then I should get a not found error

Scenario: Viewing a private activity, not authorized
  Given I am logged in
  And there is a private activity
  When I go to the page for that activity
  Then I should get a not found error

Scenario: Viewing a private activity, authorized
  Given I am logged in
  And there is a private activity
  And I am authorized to view that activity
  When I go to the page for that activity
  Then I should see that activity
