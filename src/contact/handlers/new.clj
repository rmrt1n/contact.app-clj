(ns contact.handlers.new
  (:require [ring.util.response :as res]
            [contact.templates :as templates]
            [contact.models :as models]))

(defn new-page [contact errors]
  (let [{:keys [first last email phone]} contact]
    (templates/layout
     [:form {:method "post"
             :action "/contacts/new"
             :style {:max-width "24rem"}}
      [:fieldset
       [:legend "contact values"]
       [:div
        [:label {:for "email"} "email"]
        [:input#email {:name "email"
                       :type "email"
                       :required true
                       :placeholder "johndoe@e.com"
                       :value (or email "")}]
        [:span.danger (or (:email errors) "")]]
       [:div
        [:label {:for "first"} "first name"]
        [:input#first {:name "first"
                       :required true
                       :placeholder "john"
                       :value (or first "")}]
        [:span.danger (or (:first errors) "")]]
       [:div
        [:label {:for "last"} "last name"]
        [:input#last {:name "last"
                      :required true
                      :placeholder "doe"
                      :value (or last "")}]
        [:span.danger (or (:last errors) "")]]
       [:div
        [:label {:for "phone"} "phone number"]
        [:input#phone {:name "phone"
                       :required true
                       :placeholder "123456789"
                       :value (or phone "")}]
        [:span.danger (or (:phone errors) "")]]
       [:button "save"]]]
     [:p [:a {:href "/contacts"} "back"]])))

(defn new-contact [db form-params]
  (let [{:strs [email first last phone]} form-params]
    {:id (-> @db count inc)
     :email email
     :first first
     :last last
     :phone phone}))

(defn new-handler [_]
  (res/response (str (new-page nil nil))))

(defn post-new-handler [{:keys [db form-params]}]
  (let [contact (new-contact db form-params)]
    (if (models/valid-contact? @db contact)
      (do
        (swap! db conj contact)
        (res/redirect "/contacts" 303))
      (res/response
       (str (new-page contact {:email "email is already used"}))))))
