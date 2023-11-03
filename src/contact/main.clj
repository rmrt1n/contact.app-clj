(ns contact.main
  (:require [ring.adapter.jetty :as jetty]
            [contact.router :as router]))

(defonce server (atom nil))

(defn start-server []
  (reset! server
          (jetty/run-jetty #'router/routes {:port 3000 :join? false})))

(defn stop-server []
  (when-some [s @server]
    (.stop s)
    (reset! server nil)))

(defn -main [& _]
  (start-server))

(comment
  (start-server)
  (stop-server))
