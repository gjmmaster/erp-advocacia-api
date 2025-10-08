(defproject juridico-api "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [ring/ring-core "1.9.5"]
                 [ring/ring-jetty-adapter "1.9.5"]
                 [metosin/reitit "0.5.18"]
                 [metosin/muuntaja "0.6.8"]
                 [buddy/buddy-sign "3.4.333"]
                 [buddy/buddy-hashers "1.8.158"]
                 [buddy/buddy-core "1.11.423"]
                 [clj-http "3.12.3"]
                 [com.github.seancorfield/next.jdbc "1.3.894"]
                 [org.postgresql/postgresql "42.7.3"]
                 [cheshire "5.12.0"]
                 [environ "1.2.0"]
                 [ring-cors "0.1.13"]]
  :main juridico.api.core
  :repl-options {:init-ns juridico.api.core}
  :test-paths ["test"]
  :profiles {:test {:dependencies [[ring/ring-mock "0.4.0"]]}})
