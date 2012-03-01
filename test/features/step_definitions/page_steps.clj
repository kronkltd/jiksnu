(use 'jiksnu.features-helper)

(Before
 (before-hook))

(After
 (after-hook))


;; Given

(Given #"^a domain exists$"                                         a-domain-exists)
(Given #"^a normal user is logged in$"                              a-normal-user-is-logged-in)
(Given #"^a user exists$"                                           a-user-exists)
(Given #"^a user exists with the password \"([^\"]+)\"$"            a-user-exists-with-password)
(Given #"^I am at the (.+) page$"                                   be-at-the-page)
(Given #"^I am logged in$"                                          a-normal-user-is-logged-in)
(Given #"^I am logged in as an admin$"                              an-admin-is-logged-in)
(Given #"^I am not logged in$"                                      am-not-logged-in)
(Given #"^there is a (.+) activity$"                                there-is-an-activity)
(Given #"^that user posts an activity$"                             user-posts-activity)

;; When

(When #"^a new activity gets posted$"                               activity-gets-posted)
(When #"^I click the \"([^\"]+)\" link$"                            do-click-link)
(When #"^I click the \"([^\"]*)\" button$"                          do-click-button)
(When #"^I click the \"([^\"]*)\" button for that domain$"          do-click-button-for-domain)
(When #"^I go to the \"([^\"]+)\" page$"                            go-to-the-page)
(When #"^I go to the \"([^\"]+)\" page for that activity$"          go-to-the-page-for-activity)
(When #"^I go to the \"([^\"]+)\" page for that domain$"            go-to-the-page-for-domain)
(When #"^I go to the \"([^\"]+)\" page for that user$"              go-to-the-page-for-user)
(When #"^I go to the \"([^\"]+)\" page for that user with a \"([^\"]+)\" format$" go-to-the-page-for-user-with-format)
(When #"^I put my password in the \"password\" field$"              do-enter-password)
(When #"^I put my username in the \"username\" field$"              do-enter-username)
(When #"^I request the host-meta page with a client$"               fetch-user-meta-for-user-with-client)
(When #"^I request the oembed resource for that activity$"          request-oembed-resource)
(When #"^I request the user-meta page for that user$"               fetch-user-meta-for-user)
(When #"^I request the user-meta page for that user with a client$" request-user-meta)
(When #"^I request the \"([^\"]+)\" page for that user with a \"([^\"]+)\" format$" request-page-for-user)
(When #"^I request the \"([^\"]*)\" stream$"                        request-stream)
(When #"^I type \"(.*)\" into the \"(.*)\" field$"                  do-enter-field)

;; Then

(Then #"^I should be an admin"                                      should-be-admin)
(Then #"^I should be at the \"([^\"]+)\" for that domain$"          be-at-the-page-for-domain)
(Then #"^I should be at the \"([^\"]+)\" page$"                     should-be-at-page)
(Then #"^I should be logged in$"                                    should-be-logged-in)
(Then #"^I should get a \"([^\"]*)\" document$"                     should-get-a-document-of-type)
(Then #"^I should get a not found error$"                           get-not-found-error)
(Then #"^I should not be logged in$"                                should-not-be-logged-in)
(Then #"^I should not see the class \"(.*)\"$"                      should-not-see-class)
(Then #"^I should receive a message from the stream$"               should-receive-activity)
(Then #"^I should receive an oEmbed document$"                      should-receive-oembed)
(Then #"^I should see a domain named \"(.*)\"$"                     should-see-domain-named)
(Then #"^I should see a form$"                                      should-see-form)
(Then #"^I should see a list of (.*)"                               should-see-list)
(Then #"^I should see a subscription list$"                         should-see-subscription-list)
(Then #"^I should see an activity"                                  should-see-a-activity)
(Then #"^I should see that activity"                                should-see-activity)
(Then #"^I should see that domain$"                                 should-see-domain)
(Then #"^I should wait$"                                            do-wait)
(Then #"^I should wait forever$"                                    do-wait-forever)
(Then #"^it should have a \"([^\"]+)\" field$"                      should-have-field)
(Then #"^log the response$"                                         log-response)
(Then #"^that domain should be deleted"                             domain-should-be-deleted)
(Then #"^that domain should be discovered$"                         domain-should-be-discovered)
(Then #"^that user's name should be \"(.*)\"$"                      name-should-be)
(Then #"^the alias field matches that user's uri$"                  alias-should-match-uri)
(Then #"^the content-type is \"([^\"]+)\""                          should-have-content-type)
(Then #"^the host field matches the current domain$"                host-field-should-match-domain)
(Then #"^the response is a redirect$"                               response-should-be-redirect)
(Then #"^the response is sucsessful"                                response-should-be-sucsessful)
