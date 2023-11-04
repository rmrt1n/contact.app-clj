(ns contact.handlers.delete
  (:require [ring.util.response :as res]))

(defn delete-handler [{:keys [db] :as req}]
  (let [contact-id (-> req
                       (get-in [:path-params :contact-id])
                       Integer/parseInt)]
    (swap! db (fn [db] (filterv #(not= contact-id (:id %)) db)))
    (res/redirect "/contacts" 303)))
