(ns contact.handlers.show
  (:require [ring.util.response :as res]
            [contact.templates :as templates]
            [contact.models :as models]))

(defn show-page [contact]
  (let [{:keys [id first last email phone]} contact]
    (templates/layout
     [:p [:strong first " " last]]
     [:div
      [:p "email: " email]
      [:p "phone: " phone]]
     [:p
      [:a {:href (str "/contacts/" id "/edit")} "edit"]
      [:a {:href "/contacts" :style {:margin-left ".5rem"}} "back"]])))

(defn show-handler [{:keys [db] :as req}]
  (let [contact-id (-> req
                       (get-in [:path-params :contact-id])
                       Integer/parseInt)
        contact    (models/get-contact-by-id @db contact-id)]
    (if (nil? contact)
      (res/not-found "not found")
      (res/response (str (show-page contact))))))
