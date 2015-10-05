(ns jiksnu.helpers-test
  (:require [jiksnu.helpers :as helpers]
            purnam.test)
  (:use-macros [purnam.test :only [fact facts]]))

(facts [[{:doc "hyphen-case"}]]
  (helpers/hyphen-case "FeedSource") => "feed-source"
  (helpers/hyphen-case "Feed") => "feed")

