(ns contact.handlers.edit
  (:require [ring.util.response :as res]
            [contact.templates :as templates]
            [contact.models :as models]))

(defn edit-page [contact errors]
  (let [{:keys [id first last email phone]} contact]
    (templates/layout
     [:form {:method "post"
             :action (str "/contacts/" id "/edit")
             :style {:max-width "24rem"}}
      [:fieldset
       [:legend "contact values"]
       [:div
        [:label {:for "email"} "email"]
        [:input#email {:name "email"
                       :type "email"
                       :required true
                       :placeholder "johndoe@e.com"
                       :value email}]
        [:span (or (:email errors) "")]]
       [:div
        [:label {:for "first"} "first name"]
        [:input#first {:name "first"
                       :required true
                       :placeholder "john"
                       :value first}]
        [:span (or (:first errors) "")]]
       [:div
        [:label {:for "last"} "last"]
        [:input#last {:name "last"
                      :required true
                      :placeholder "doe"
                      :value last}]
        [:span (or (:last errors) "")]]
       [:div
        [:label {:for "phone"} "phone number"]
        [:input#phone {:name "phone"
                       :required true
                       :placeholder "123456789"
                       :value phone}]
        [:span (or (:phone errors) "")]]
       [:button "save"]]]
     [:form {:method "post"
             :action (str "/contacts/" id "/delete")}
      [:button "delete contact"]]
     [:p [:a {:href "/contacts"} "back"]])))

(defn edit-handler [{:keys [db] :as req}]
  (let [contact-id (-> req (get-in [:path-params :contact-id]) Integer/parseInt)
        contact (models/get-contact-by-id @db contact-id)]
    (if (nil? contact)
      (res/not-found "not found")
      (res/response (str (edit-page contact nil))))))

(defn post-edit-handler [{:keys [form-params db] :as req}]
  (let [contact-id (-> req (get-in [:path-params :contact-id]) Integer/parseInt)
        old-contact (models/get-contact-by-id @db contact-id)
        {:strs [email first last phone]} form-params
        new-contact (assoc old-contact
                           :email email
                           :first first
                           :last last
                           :phone phone)]
    (println "this fuck" new-contact)
    (if (models/valid-contact? @db new-contact)
      (do
        (swap! db assoc (dec contact-id) new-contact)
        (res/redirect (str "/contacts/" contact-id) 303))
      (res/response
       (str (edit-page new-contact {:email "email is already used"}))))))
