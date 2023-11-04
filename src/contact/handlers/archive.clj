(ns contact.handlers.archive
  (:require [ring.util.response :as res]
            [hiccup2.core :as h]))

(defn archive-fragment []
  (h/html
   [:div {:hx-target "this"
          :hx-swap "outerHTML"
          :id "archive-ui"}
    [:button {:hx-post "/contacts/archive"} "download contact archive"]]))

(defn archive-handler [req]
  (res/response "hi there"))
