(ns jiksnu.commands
  (:use  [ciste.commands :only [add-command! command-names]]
         [ciste.core :only [serialize-as]]
         [ciste.filters :only [deffilter]]
         [ciste.views :only [defview]])
  (:require [ciste.workers :as workers]
))

(add-command! "help" #'command-names)

