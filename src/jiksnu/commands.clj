(ns jiksnu.commands
  (:use  [ciste.commands :only [add-command! command-names]]))

(add-command! "help" #'command-names)
