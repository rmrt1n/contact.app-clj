(ns contact.handlers.contacts
  (:require [ring.util.response :as res]
            [clojure.string :as str]
            [contact.templates :as templates]))

(defn contacts-page [q contacts]
  (templates/layout
   [:form.space-y-2 {:method "get"
                     :action "/contacts"
                     :style {:max-width "24rem"}}
    [:label {:for "search"} "search term"]
    [:input#search {:name "q" :type "search" :value q}]
    [:button "search"]]
   [:table
    [:thead
     [:tr [:th "first"] [:th "last"] [:th "phone"] [:th "email"] [:th]]]
    [:tbody
     (map (fn [c] [:tr
                   [:td (:first c)]
                   [:td (:last c)]
                   [:td (:phone c)]
                   [:td (:email c)]
                   [:td
                    [:a {:href (str "/contacts/" (:id c) "/edit")} "edit"]
                    [:a {:href (str "/contacts/" (:id c))
                         :style {:margin-left ".5rem"}}
                     "view"]]])
          contacts)]]
   [:p [:a {:href "/contacts/new"} "add contact"]]))

(defn contacts-search [contacts text]
  (let [re (re-pattern text)]
    (filterv (fn [c] (or (not (nil? (re-find re (str (:id c)))))
                         (not (nil? (re-find re (:first c))))
                         (not (nil? (re-find re (:last c))))
                         (not (nil? (re-find re (:phone c))))
                         (not (nil? (re-find re (:email c))))))
             contacts)))

(defn contacts-handler [{:keys [db] :as req}]
  (let [q (-> req :query-params (get "q"))
        contacts (if (nil? q)
                   @db
                   (contacts-search @db (str/lower-case q)))]
    (res/response (str (contacts-page q contacts)))))

