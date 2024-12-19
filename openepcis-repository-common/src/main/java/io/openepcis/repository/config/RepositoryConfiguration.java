/*
 * Copyright 2022-2024 benelog GmbH & Co. KG
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package io.openepcis.repository.config;

import io.quarkus.runtime.annotations.StaticInitSafe;
import io.smallrye.config.ConfigMapping;

@StaticInitSafe
@ConfigMapping(prefix = "repository")
public interface RepositoryConfiguration {

    Epcis epcis();

    User user();

    Capture capture();

    Epc epc();

    interface Epc {
        String index();
    }

    interface Epcis {
        Event event();

        Query query();

        Subscription subscription();

        Streaming streaming();

        String nextPageTokenTTL();

        Integer perPage();


        interface Event {
            String index();
        }

        interface Query {
            String index();
        }

        interface Subscription {
            String index();
        }

        interface Streaming {
            String subscription();
        }
    }

    interface Capture {
        Job job();

        interface Job {
            String index();
        }
    }

    interface User {
        Extension extension();

        interface Extension {
            Fields fields();

            Schema schema();

            interface Fields {
                String index();
            }

            interface Schema {
                String index();
            }
        }
    }
}
