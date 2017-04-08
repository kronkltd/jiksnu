(ns jiksnu.World
  (:require [cljs.nodejs :as nodejs]
            [jiksnu.PageObjectMap :refer [pages]]))

(def World (.-World (nodejs/require "cukefarm")))
(set! (.. World -prototype -pageObjectMap) pages)
(set! (.. js/module -exports -World) World)
