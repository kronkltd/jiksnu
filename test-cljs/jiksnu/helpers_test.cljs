(ns jiksnu.helpers-test
  (:require [jiksnu.helpers :as helpers])
  (:use-macros [purnam.test :only [describe is it beforeEach]]))

(describe {:doc "jiksnu.helpers"}
  (describe {:doc "hyphen-case"}
    (it "handles multi-parts"
      (is (helpers/hyphen-case "FeedSource") "feed-source"))

    (it "handles single parts"
      (is (helpers/hyphen-case "Feed") "feed"))))
