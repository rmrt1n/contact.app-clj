(ns contact.handlers.validate
  (:require [ring.util.response :as res]))

(defn email-handler [{:keys [db query-params] :as req}]
  (let [contact-id (-> req
                       (get-in [:path-params :contact-id])
                       Integer/parseInt)
        email      (get query-params "email")
        existing   (filter #(= email (:email %)) @db)
        response   (if (empty? existing) "" "email is already used")]
    (res/response response)))
