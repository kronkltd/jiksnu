(ns jiksnu.app
  (:require [jiksnu.helpers :as helpers])
  (:use-macros [gyr.core :only [def.module def.config]]
               [purnam.core :only [? ?> ! !> f.n def.n do.n
                                   obj arr def* do*n def*n f*n]]))

(def.module jiksnu [ui.router ui.bootstrap angularMoment
                    ui.bootstrap.tabs])

