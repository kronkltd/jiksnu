Feature: Activities
  In order to have full control over activities
  As a user
  I want to be able to perform management tasks

@focus
Scenario: Viewing a public activity, unauthenticated
  Given I am not logged in
  And there is a public activity
  When I go to the "show" page for that activity
  Then I should see that activity

Scenario: Viewing a private activity, unauthenticated
  Given I am not logged in
  And there is a private activity
  When I go to the "show" page for that activity
  Then I should get a not found error

Scenario: Viewing a private activity, not authorized
  Given I am logged in
  And there is a private activity
  When I go to the "show" page for that activity
  Then I should get a not found error

Scenario: Viewing a private activity, authorized
  Given I am logged in
  And there is a private activity
  And I am authorized to view that activity
  When I go to the page for that activity
  Then I should see that activity

Scenario: Posting an activity, unauthenticated
  Given I am not logged in
  When I go to the "home" page
  Then I should not see the class "post-form"

Scenario: Posting an activity, normal user
  Given I am logged in
  And I am at the "home" page
  When I type "foobar" into the "content" field
  And I submit that form
  Then that activity should be created
