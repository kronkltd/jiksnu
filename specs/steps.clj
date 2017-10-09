(require '[jiksnu.helpers.actions :as helpers.action])
(require '[jiksnu.helpers.features :as helpers.features])
(require '[jiksnu.helpers.navigation :as helpers.navigation])
(require '[taoensso.timbre :as timbre])

(Before [] (helpers.features/before-hook))
(After [] (helpers.features/after-hook))

;; Given

(Given #"^a user exists with the password \"(.*?)\"$" [password]
  (helpers.action/register-user password))

(Given #"^I am (not )?logged in$" [not-str]
  (if (empty? not-str)
    (helpers.action/login-user)
    nil #_(helpers.action/log-out!)))

;; When

(When #"^I go to the \"([^\"]*)\" page$" [page-name]
  (helpers.navigation/go-to-the-page page-name))

;; Then

(Then #"^I should see a form$" []
  ;; TODO: Test if a form exists
  (timbre/info "Testing if a form exists"))

(Then #"^it should have a \"(.*?)\" field$" [arg1]
      (comment  Write code here that turns the phrase above into concrete actions  )
      (throw (cucumber.api.PendingException.)))
