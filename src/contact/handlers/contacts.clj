(ns contact.handlers.contacts
  (:require [ring.util.response :as res]
            [clojure.string :as str]
            [contact.templates :as templates]))

(defn contacts-page [q contacts page]
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
   [:div
    (when (> page 1)
      [:a.btn {:href (str "?page=" (dec page)
                          (when (not (nil? q))
                            (str "&?q=" q)))
               :style {:margin-right ".5rem"}} "prev"])
    (when (= 10 (count contacts))
      [:a.btn {:href (str "?page=" (inc page)
                          (when (not (nil? q))
                            (str "&?q=" q)))} "next"])]
   [:p [:a {:href "/contacts/new"} "add contact"]]))

(defn contacts-search [contacts text]
  (let [re (re-pattern text)]
    (filterv (fn [c] (or (not (nil? (re-find re (str (:id c)))))
                         (not (nil? (re-find re (:first c))))
                         (not (nil? (re-find re (:last c))))
                         (not (nil? (re-find re (:phone c))))
                         (not (nil? (re-find re (:email c))))))
             contacts)))

(defn contacts-handler [{:keys [db query-params]}]
  (let [q        (get query-params "q")
        page     (-> (or (get query-params "page") "1") Integer/parseInt)
        contacts (if (nil? q)
                   @db
                   (contacts-search @db (str/lower-case q)))
        paged    (if (> (+ 10 page) (count contacts))
                   (subvec contacts page)
                   (subvec contacts page (+ 10 page)))]
    (res/response
     (str (contacts-page q paged page)))))
