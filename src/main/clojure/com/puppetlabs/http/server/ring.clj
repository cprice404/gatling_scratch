(ns com.puppetlabs.http.server.ring
  (:gen-class)
  (:require [ring.adapter.jetty :as jetty]))

(defn handler [request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body "Hello World"})

(defn -main
  [& args]
  (jetty/run-jetty handler
    { :port             8000
      :ssl-port         8140
      :keystore         "./src/main/resources/com/puppetlabs/http/server/jetty.keystore"
      :key-password     "puppet"
      :truststore       "./src/main/resources/com/puppetlabs/http/server/jetty.keystore"
      :trust-password   "puppet"
    }))