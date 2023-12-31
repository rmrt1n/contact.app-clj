(ns contact.templates
  (:require [hiccup2.core :as h]))

(defn layout [& children]
  (h/html
   [:head
    [:title "contact.app"]
    [:meta {:charset "utf-8"
            :name    "viewport"
            :content "width=device-width, initial-scale=1.0"}]
    [:link {:rel "stylesheet"
            :href "https://www.unpkg.com/modern-normalize"}]
    [:link {:rel "stylesheet"
            :href "https://www.unpkg.com/clam.css"}]
    [:link {:rel "stylesheet" :href "/css/style.css"}]
    [:script {:src "https://unpkg.com/htmx.org"}]
    [:script {:src "https://unpkg.com/hyperscript.org"}]
    [:script {:src "https://unpkg.com/alpinejs" :defer true}]
    [:script {:src "/js/script.js" :type "module" :defer true}]]
   [:body {:hx-boost "true"}
    [:header
     [:nav
      [:a {:href "/"} "contact.app"]]]
    [:main
     children]]))
