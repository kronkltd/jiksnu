(require '[jiksnu.helpers.actions :as helpers.action])
(require '[jiksnu.helpers.features :as helpers.features])
(require '[jiksnu.helpers.navigation :as helpers.navigation])

(Before [] (helpers.features/before-hook))
(After [] (helpers.features/after-hook))

(Given #"^a user exists with the password \"(.*?)\"$" [password]
  (helpers.action/register-user password))

(When #"^I go to the \"([^\"]*)\" page$" [page-name]
  (helpers.navigation/go-to-the-page page-name))
