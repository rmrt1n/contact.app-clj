(ns contact.router
  (:require [reitit.ring :as ring]
            [reitit.ring.middleware.parameters :as params]
            [ring.util.response :as res]
            [contact.handlers.contacts :as contacts]
            [contact.handlers.new :as new]
            [contact.handlers.show :as show]
            [contact.handlers.edit :as edit]
            [contact.handlers.delete :as delete]
            [contact.models :as models]))

(defonce db (atom models/contacts))

#_(swap! db (constantly models/contacts))

(defn index [_]
  (res/response "hi world"))

(def middleware-db
  {:name ::db
   :compile (fn [{:keys [db]} _]
              (fn [handler]
                (fn [req]
                  (handler (assoc req :db db)))))})

(def routes
  (ring/ring-handler
   (ring/router
    [["/" {:get (fn [] (res/redirect "/contacts"))}]
     ["/contacts"
      [""         {:get contacts/contacts-handler}]
      ["/new"     {:get  new/new-handler
                   :post new/post-new-handler}]
      ["/:contact-id"
       [""        {:get show/show-handler}]
       ["/edit"   {:get  edit/edit-handler
                   :post edit/post-edit-handler}]
       ["/delete" {:post delete/delete-handler}]]]]
    {:data {:db db
            :middleware [params/parameters-middleware
                         middleware-db]}
     :conflicts nil})
   (ring/routes
    (ring/create-resource-handler {:path "/"})
    (ring/create-default-handler
     {:not-found (fn [_] {:status 404 :body "not found"})}))))
