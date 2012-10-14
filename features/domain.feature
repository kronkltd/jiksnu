Feature: Domains
  In order to have full control over activities
  As a user
  I want to be able to perform management tasks

  # Scenario: Domain index page
  #   Given I am logged in as an admin
  #   When I go to the "domain index" page
  #   Then I should see a list of domains

  # Scenario: Show domain page
  #   Given I am logged in as an admin
  #   And a domain exists
  #   When I go to the "show" page for that domain
  #   # Then I should wait forever
  #   Then I should see that domain
    
  # Scenario: Deleting a domain
  #   Given I am logged in as an admin
  #   And a domain exists
  #   And I am at the "domain index" page
  #   When I click the "delete" button for that domain
  #   Then I should be at the "domain index" page
  #   And that domain should be deleted
    
  # Scenario: Discovering a domain
  #   Given I am logged in as an admin
  #   And a domain exists
  #   And I am at the "domain index" page
  #   When I click the "discover" button
  #   Then I should be at the "show" for that domain
  #   And that domain should be discovered
    
  # Scenario: Adding a domain
  #   Given I am logged in as an admin
  #   And I am at the "domain index" page
  #   When I type "example.com" into the "domain" field
  #   And I click the "add" button
  #   Then I should be at the "domain index" page
  #   And I should see a domain named "example.com"
    
  # Scenario: Fetching a Host Meta document
  #   Given I am not logged in
  #   When I request the host-meta page with a client
  #   Then the host field matches the current domain
  #   And the content-type is "application/xrds+xml"
  #   And the response is sucsessful
