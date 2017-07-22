(require '[jiksnu.helpers.actions :as helpers.action])
(require '[jiksnu.helpers.features :as helpers.features])
(require '[jiksnu.helpers.navigation :as helpers.navigation])

(Before [] (helpers.features/before-hook))
(After [] (helpers.features/after-hook))

(Given #"^a user exists with the password \"(.*?)\"$" [password]
  (helpers.action/register-user password))

(Given #"^I am (not )?logged in$" [not-str]
  (if (empty? not-str)
    (helpers.action/login-user)
    nil #_(helpers.action/log-out!)))

(When #"^I go to the \"([^\"]*)\" page$" [page-name]
  (helpers.navigation/go-to-the-page page-name))
