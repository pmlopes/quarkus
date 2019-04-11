/*
 * Copyright 2018 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.quarkus.example.panache;

import static com.ea.async.Async.await;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.junit.jupiter.api.Assertions;
import org.reactivestreams.Publisher;

import io.quarkus.coroutines.eaasync.Suspendable;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import io.quarkus.panache.rx.PanacheRxQuery;

/**
 * Various tests covering Panache functionality. All tests should work in both standard JVM and SubstrateVM.
 */
@Path("test")
public class TestEndpoint {

    private <T> CompletionStage<List<T>> toList(Publisher<T> publisher) {
        return ReactiveStreams.fromPublisher(publisher).toList().run();
    }

    @Suspendable
    @GET
    @Path("rx-model")
    public CompletionStage<String> testRxModel() {
        List<RxPerson> persons = await(RxPerson.findAll().list());
        
        Assertions.assertEquals(0, persons.size());

        persons = await(RxPerson.listAll());
        Assertions.assertEquals(0, persons.size());

        persons = await(toList(RxPerson.findAll().stream()));
        Assertions.assertEquals(0, persons.size());

        persons = await(toList(RxPerson.streamAll()));
        Assertions.assertEquals(0, persons.size());

        try {
            await(RxPerson.findAll().singleResult());
            Assertions.fail();
        }catch(Exception x) {
            Assertions.assertTrue(x instanceof CompletionException);
            Assertions.assertTrue(x.getCause() instanceof NoResultException);
        }

        RxPerson person = await(RxPerson.findAll().firstResult());
        Assertions.assertNull(person);

        person = await(makeSavedRxPerson());
        Assertions.assertNotNull(person.id);

        long count = await(RxPerson.count());
        Assertions.assertEquals(1, count);

        count = await(RxPerson.count("name = ?1", "stef"));
        Assertions.assertEquals(1, count);

        count = await(RxPerson.count("name = :name", Parameters.with("name", "stef").map()));
        Assertions.assertEquals(1, count);

        count = await(RxPerson.count("name = :name", Parameters.with("name", "stef")));
        Assertions.assertEquals(1, count);

        count = await(RxPerson.count("name", "stef"));
        Assertions.assertEquals(1, count);

        count = await(RxDog.count());
        Assertions.assertEquals(1, count);

        count = await(toList(person.dogs)).size();
        Assertions.assertEquals(1, count);

        persons = await(RxPerson.findAll().list());
        Assertions.assertEquals(1, persons.size());
        Assertions.assertEquals(person, persons.get(0));

        persons = await(RxPerson.listAll());
        Assertions.assertEquals(1, persons.size());
        Assertions.assertEquals(person, persons.get(0));

        persons = await(toList(RxPerson.findAll().stream()));
        Assertions.assertEquals(1, persons.size());
        Assertions.assertEquals(person, persons.get(0));

        persons = await(toList(RxPerson.streamAll()));
        Assertions.assertEquals(1, persons.size());
        Assertions.assertEquals(person, persons.get(0));

        RxPerson p2 = await(RxPerson.findAll().firstResult());
        Assertions.assertEquals(person, p2);

        p2 = await(RxPerson.findAll().singleResult());
        Assertions.assertEquals(person, p2);

        persons = await(RxPerson.find("name = ?1", "stef").list());
        Assertions.assertEquals(1, persons.size());
        Assertions.assertEquals(person, persons.get(0));

        persons = await(RxPerson.list("name = ?1", "stef"));
        Assertions.assertEquals(1, persons.size());
        Assertions.assertEquals(person, persons.get(0));

        persons = await(RxPerson.find("name = :name", Parameters.with("name", "stef").map()).list());
        Assertions.assertEquals(1, persons.size());
        Assertions.assertEquals(person, persons.get(0));

        persons = await(RxPerson.find("name = :name", Parameters.with("name", "stef")).list());
        Assertions.assertEquals(1, persons.size());
        Assertions.assertEquals(person, persons.get(0));

        persons = await(RxPerson.list("name = :name", Parameters.with("name", "stef").map()));
        Assertions.assertEquals(1, persons.size());
        Assertions.assertEquals(person, persons.get(0));

        persons = await(RxPerson.list("name = :name", Parameters.with("name", "stef")));
        Assertions.assertEquals(1, persons.size());
        Assertions.assertEquals(person, persons.get(0));

        persons = await(RxPerson.find("name", "stef").list());
        Assertions.assertEquals(1, persons.size());
        Assertions.assertEquals(person, persons.get(0));

        persons = await(toList(RxPerson.find("name = ?1", "stef").stream()));
        Assertions.assertEquals(1, persons.size());
        Assertions.assertEquals(person, persons.get(0));

        persons = await(toList(RxPerson.stream("name = ?1", "stef")));
        Assertions.assertEquals(1, persons.size());
        Assertions.assertEquals(person, persons.get(0));

        persons = await(toList(RxPerson.find("name = :name", Parameters.with("name", "stef").map()).stream()));
        Assertions.assertEquals(1, persons.size());
        Assertions.assertEquals(person, persons.get(0));

        persons = await(toList(RxPerson.find("name = :name", Parameters.with("name", "stef")).stream()));
        Assertions.assertEquals(1, persons.size());
        Assertions.assertEquals(person, persons.get(0));

        persons = await(toList(RxPerson.stream("name = :name", Parameters.with("name", "stef").map())));
        Assertions.assertEquals(1, persons.size());
        Assertions.assertEquals(person, persons.get(0));

        persons = await(toList(RxPerson.stream("name = :name", Parameters.with("name", "stef"))));
        Assertions.assertEquals(1, persons.size());
        Assertions.assertEquals(person, persons.get(0));

        persons = await(toList(RxPerson.find("name", "stef").stream()));
        Assertions.assertEquals(1, persons.size());
        Assertions.assertEquals(person, persons.get(0));

        p2 = await(RxPerson.find("name", "stef").firstResult());
        Assertions.assertEquals(person, p2);

        p2 = await(RxPerson.find("name", "stef").singleResult());
        Assertions.assertEquals(person, p2);

        p2 = await(RxPerson.findById(person.id));
        Assertions.assertEquals(person, p2);

        await(person.delete());
        count = await(RxPerson.count());
        Assertions.assertEquals(0, count);

        // Difference with JPA: no cascade
        count = await(RxDog.deleteAll());
        Assertions.assertEquals(1, count);

        RxPerson newPerson = await(makeSavedRxPerson());
        count = await(RxPerson.count());
        Assertions.assertEquals(1, count);

        count = await(RxPerson.delete("name = ?1", "emmanuel"));
        Assertions.assertEquals(0, count);

        // FIXME: translate to owner_id
        count = await(RxDog.delete("owner_id = ?1", newPerson.id));
        Assertions.assertEquals(1, count);

        count = await(RxPerson.delete("name", "stef"));
        Assertions.assertEquals(1, count);

        newPerson = await(makeSavedRxPerson());
        // FIXME: translate to owner_id
        count = await(RxDog.delete("owner_id = :owner", Parameters.with("owner", newPerson.id).map()));
        Assertions.assertEquals(1, count);

        count = await(RxPerson.delete("name", "stef"));
        Assertions.assertEquals(1, count);

        newPerson = await(makeSavedRxPerson());
        // FIXME: translate to owner_id
        count = await(RxDog.delete("owner_id = :owner", Parameters.with("owner", newPerson.id)));
        Assertions.assertEquals(1, count);

        count = await(RxPerson.delete("name", "stef"));
        Assertions.assertEquals(1, count);

        count = await(RxPerson.deleteAll());
        Assertions.assertEquals(0, count);

        newPerson = await(makeSavedRxPerson());
        count = await(RxDog.deleteAll());
        Assertions.assertEquals(1, count);

        count = await(RxPerson.deleteAll());
        Assertions.assertEquals(1, count);

        await(testPersist(PersistTest.Iterable));
        await(testPersist(PersistTest.Stream));
        await(testPersist(PersistTest.Variadic));
        
        count = await(RxPerson.deleteAll());
        Assertions.assertEquals(6, count);

        await(testSorting());
        // paging
        for (int i = 0; i < 7; i++) {
            await(makeSavedRxPerson(String.valueOf(i)));
        }
        
        await(testPaging(RxPerson.findAll()));
        await(testPaging(RxPerson.find("ORDER BY name")));
        try {
            await(RxPerson.findAll().singleResult());
            Assertions.fail();
        }catch(Exception x) {
            Assertions.assertTrue(x instanceof CompletionException);
            Assertions.assertTrue(x.getCause() instanceof NonUniqueResultException);
        }
        person = await(RxPerson.findAll().firstResult());
        Assertions.assertNotNull(person);

        count = await(RxPerson.deleteAll());
        Assertions.assertEquals(7, count);

        return CompletableFuture.completedFuture("OK");
    }

    private CompletionStage<Void> testPaging(PanacheRxQuery<RxPerson> query) {
        // ints
        return query.page(0, 3).list()
                .thenCompose(persons -> {
                    Assertions.assertEquals(3, persons.size());
                    Assertions.assertEquals("stef0", persons.get(0).name);
                    Assertions.assertEquals("stef1", persons.get(1).name);
                    Assertions.assertEquals("stef2", persons.get(2).name);

                    return query.page(1, 3).list();
                }).thenCompose(persons -> {
                    Assertions.assertEquals(3, persons.size());
                    Assertions.assertEquals("stef3", persons.get(0).name);
                    Assertions.assertEquals("stef4", persons.get(1).name);
                    Assertions.assertEquals("stef5", persons.get(2).name);

                    return query.page(2, 3).list();
                }).thenCompose(persons -> {
                    Assertions.assertEquals(1, persons.size());
                    Assertions.assertEquals("stef6", persons.get(0).name);

                    return query.page(2, 4).list();
                }).thenCompose(persons -> {
                    Assertions.assertEquals(0, persons.size());

                    // page
                    Page page = new Page(3);
                    return query.page(page).list();
                }).thenCompose(persons -> {
                    Assertions.assertEquals(3, persons.size());
                    Assertions.assertEquals("stef0", persons.get(0).name);
                    Assertions.assertEquals("stef1", persons.get(1).name);
                    Assertions.assertEquals("stef2", persons.get(2).name);

                    Page page = new Page(3).next();
                    return query.page(page).list();
                }).thenCompose(persons -> {
                    Assertions.assertEquals(3, persons.size());
                    Assertions.assertEquals("stef3", persons.get(0).name);
                    Assertions.assertEquals("stef4", persons.get(1).name);
                    Assertions.assertEquals("stef5", persons.get(2).name);

                    Page page = new Page(3).next().next();
                    return query.page(page).list();
                }).thenCompose(persons -> {
                    Assertions.assertEquals(1, persons.size());
                    Assertions.assertEquals("stef6", persons.get(0).name);

                    Page page = new Page(3).next().next().next();
                    return query.page(page).list();
                }).thenCompose(persons -> {
                    Assertions.assertEquals(0, persons.size());

                    // query paging
                    Page page = new Page(3);
                    return query.page(page).list();
                }).thenCompose(persons -> {
                    Assertions.assertEquals(3, persons.size());
                    Assertions.assertEquals("stef0", persons.get(0).name);
                    Assertions.assertEquals("stef1", persons.get(1).name);
                    Assertions.assertEquals("stef2", persons.get(2).name);
                    Assertions.assertFalse(query.hasPreviousPage());

                    return query.hasNextPage();
                }).thenCompose(hasNextPage -> {
                    Assertions.assertTrue(hasNextPage);

                    return query.nextPage().list();
                }).thenCompose(persons -> {
                    Assertions.assertEquals(1, query.page().index);
                    Assertions.assertEquals(3, query.page().size);
                    Assertions.assertEquals(3, persons.size());
                    Assertions.assertEquals("stef3", persons.get(0).name);
                    Assertions.assertEquals("stef4", persons.get(1).name);
                    Assertions.assertEquals("stef5", persons.get(2).name);
                    Assertions.assertTrue(query.hasPreviousPage());

                    return query.hasNextPage();
                }).thenCompose(hasNextPage -> {
                    Assertions.assertTrue(hasNextPage);

                    return query.nextPage().list();
                }).thenCompose(persons -> {
                    Assertions.assertEquals(1, persons.size());
                    Assertions.assertEquals("stef6", persons.get(0).name);
                    Assertions.assertTrue(query.hasPreviousPage());

                    return query.hasNextPage();
                }).thenCompose(hasNextPage -> {
                    Assertions.assertFalse(hasNextPage);

                    return query.nextPage().list();
                }).thenCompose(persons -> {
                    Assertions.assertEquals(0, persons.size());

                    return query.count();
                }).thenCompose(count -> {
                    Assertions.assertEquals(7, count);

                    return query.pageCount();
                }).thenApply(pageCount -> {
                    Assertions.assertEquals(3, pageCount);

                    return null;
                });
    }

    private CompletionStage<Void> testSorting() {
        RxPerson person1 = new RxPerson();
        person1.name = "stef";
        person1.status = Status.LIVING;
        return person1.save()
                .thenCompose(p1 -> {
                    RxPerson person2 = new RxPerson();
                    person2.name = "stef";
                    person2.status = Status.DECEASED;
                    return person2.save()
                            .thenCompose(p2 -> {
                                RxPerson person3 = new RxPerson();
                                person3.name = "emmanuel";
                                person3.status = Status.LIVING;
                                return person3.save();
                            }).thenCompose(p3 -> {
                                Sort sort1 = Sort.by("name", "status");
                                List<RxPerson> order1 = Arrays.asList(p3, person1, person2);
                                Sort sort2 = Sort.descending("name", "status");
                                List<RxPerson> order2 = Arrays.asList(person2, person1);

                                return RxPerson.findAll(sort1).list()
                                        .thenCompose(list -> {
                                            Assertions.assertEquals(order1, list);

                                            return RxPerson.listAll(sort1);
                                        }).thenCompose(list -> {
                                            Assertions.assertEquals(order1, list);

                                            return toList(RxPerson.streamAll(sort1));
                                        }).thenCompose(list -> {
                                            Assertions.assertEquals(order1, list);

                                            return RxPerson.find("name", sort2, "stef").list();
                                        }).thenCompose(list -> {
                                            Assertions.assertEquals(order2, list);

                                            return RxPerson.list("name", sort2, "stef");
                                        }).thenCompose(list -> {
                                            Assertions.assertEquals(order2, list);

                                            return toList(RxPerson.stream("name", sort2, "stef"));
                                        }).thenCompose(list -> {
                                            Assertions.assertEquals(order2, list);

                                            return RxPerson.find("name = :name", sort2, Parameters.with("name", "stef").map())
                                                    .list();
                                        }).thenCompose(list -> {
                                            Assertions.assertEquals(order2, list);

                                            return RxPerson.list("name = :name", sort2, Parameters.with("name", "stef").map());
                                        }).thenCompose(list -> {
                                            Assertions.assertEquals(order2, list);

                                            return toList(RxPerson.stream("name = :name", sort2,
                                                    Parameters.with("name", "stef").map()));
                                        }).thenCompose(list -> {
                                            Assertions.assertEquals(order2, list);

                                            return RxPerson.find("name = :name", sort2, Parameters.with("name", "stef")).list();
                                        }).thenCompose(list -> {
                                            Assertions.assertEquals(order2, list);

                                            return RxPerson.list("name = :name", sort2, Parameters.with("name", "stef"));
                                        }).thenCompose(list -> {
                                            Assertions.assertEquals(order2, list);

                                            return toList(
                                                    RxPerson.stream("name = :name", sort2, Parameters.with("name", "stef")));
                                        }).thenCompose(list -> {
                                            Assertions.assertEquals(order2, list);

                                            return RxPerson.deleteAll();
                                        }).thenApply(count -> {
                                            Assertions.assertEquals(3, count);

                                            return null;
                                        });
                            });
                });
    }

    enum PersistTest {
        Iterable,
        Variadic,
        Stream;
    }

    private CompletionStage<Void> testPersist(PersistTest persistTest) {
        RxPerson person1 = new RxPerson();
        person1.name = "stef1";
        RxPerson person2 = new RxPerson();
        person2.name = "stef2";
        Assertions.assertFalse(person1.isPersistent());
        Assertions.assertFalse(person2.isPersistent());
        CompletionStage<Void> persistOperation = null;
        switch (persistTest) {
            case Iterable:
                persistOperation = RxPerson.save(Arrays.asList(person1, person2));
                break;
            case Stream:
                persistOperation = RxPerson.save(Stream.of(person1, person2));
                break;
            case Variadic:
                persistOperation = RxPerson.save(person1, person2);
                break;
        }
        return persistOperation
                .thenApply(v -> {
                    Assertions.assertTrue(person1.isPersistent());
                    Assertions.assertTrue(person2.isPersistent());

                    return v;
                });
    }

    private CompletionStage<? extends RxPerson> makeSavedRxPerson(String suffix) {
        RxPerson person = new RxPerson();
        person.name = "stef" + suffix;
        person.status = Status.LIVING;

        try {
            return person.save();
        } catch (Throwable t) {
            t.printStackTrace();
            throw new RuntimeException(t);
        }
    }

    private CompletionStage<? extends RxPerson> makeSavedRxPerson() {
        return makeSavedRxPerson("")
                .thenCompose(person -> {
                    System.err.println("Got Person ID " + person.id);
                    RxDog dog = new RxDog("octave", "dalmatian");
                    dog.owner = CompletableFuture.completedFuture(person);
                    person.dogs = ReactiveStreams.of(dog).buildRs();
                    return dog.save().thenApply(v -> person);
                });
    }

    @Inject
    RxPersonRepository rxPersonRepository;
    @Inject
    RxDogDao rxDogRepository;

    @GET
    @Path("rx-model-repository")
    public CompletionStage<String> testRxModelRepository() {
        return rxPersonRepository.findAll().list()
                .thenCompose(persons -> {
                    Assertions.assertEquals(0, persons.size());

                    return rxPersonRepository.listAll();
                }).thenCompose(persons -> {
                    Assertions.assertEquals(0, persons.size());

                    return toList(rxPersonRepository.findAll().stream());
                }).thenCompose(persons -> {
                    Assertions.assertEquals(0, persons.size());

                    return toList(rxPersonRepository.streamAll());
                }).thenCompose(persons -> {
                    Assertions.assertEquals(0, persons.size());

                    return rxPersonRepository.findAll().singleResult().handle((v, x) -> x);
                }).thenCompose(x -> {
                    Assertions.assertTrue(x instanceof CompletionException);
                    Assertions.assertTrue(x.getCause() instanceof NoResultException);

                    return rxPersonRepository.findAll().firstResult();
                }).thenCompose(person -> {
                    Assertions.assertNull(person);

                    return makeSavedRxPersonRepository();
                }).thenCompose(person -> {
                    Assertions.assertNotNull(person.id);

                    return rxPersonRepository.count()
                            .thenCompose(count -> {
                                Assertions.assertEquals(1, (long) count);

                                return rxPersonRepository.count("name = ?1", "stef");
                            }).thenCompose(count -> {
                                Assertions.assertEquals(1, (long) count);

                                return rxPersonRepository.count("name = :name", Parameters.with("name", "stef").map());
                            }).thenCompose(count -> {
                                Assertions.assertEquals(1, (long) count);

                                return rxPersonRepository.count("name = :name", Parameters.with("name", "stef"));
                            }).thenCompose(count -> {
                                Assertions.assertEquals(1, (long) count);

                                return rxPersonRepository.count("name", "stef");
                            }).thenCompose(count -> {
                                Assertions.assertEquals(1, (long) count);

                                return rxDogRepository.count();
                            }).thenCompose(count -> {
                                Assertions.assertEquals(1, (long) count);

                                return toList(person.dogs).thenApply(List::size);
                            }).thenCompose(count -> {
                                Assertions.assertEquals(1, (long) count);

                                return rxPersonRepository.findAll().list();
                            }).thenCompose(persons -> {
                                Assertions.assertEquals(1, persons.size());
                                Assertions.assertEquals(person, persons.get(0));

                                return rxPersonRepository.listAll();
                            }).thenCompose(persons -> {
                                Assertions.assertEquals(1, persons.size());
                                Assertions.assertEquals(person, persons.get(0));

                                return toList(rxPersonRepository.findAll().stream());
                            }).thenCompose(persons -> {
                                Assertions.assertEquals(1, persons.size());
                                Assertions.assertEquals(person, persons.get(0));

                                return toList(rxPersonRepository.streamAll());
                            }).thenCompose(persons -> {
                                Assertions.assertEquals(1, persons.size());
                                Assertions.assertEquals(person, persons.get(0));

                                return rxPersonRepository.findAll().firstResult();
                            }).thenCompose(p2 -> {
                                Assertions.assertEquals(person, p2);

                                return rxPersonRepository.findAll().singleResult();
                            }).thenCompose(p2 -> {
                                Assertions.assertEquals(person, p2);

                                return rxPersonRepository.find("name = ?1", "stef").list();
                            }).thenCompose(persons -> {
                                Assertions.assertEquals(1, persons.size());
                                Assertions.assertEquals(person, persons.get(0));

                                return rxPersonRepository.list("name = ?1", "stef");
                            }).thenCompose(persons -> {
                                Assertions.assertEquals(1, persons.size());
                                Assertions.assertEquals(person, persons.get(0));

                                return rxPersonRepository.find("name = :name", Parameters.with("name", "stef").map()).list();
                            }).thenCompose(persons -> {
                                Assertions.assertEquals(1, persons.size());
                                Assertions.assertEquals(person, persons.get(0));

                                return rxPersonRepository.find("name = :name", Parameters.with("name", "stef")).list();
                            }).thenCompose(persons -> {
                                Assertions.assertEquals(1, persons.size());
                                Assertions.assertEquals(person, persons.get(0));

                                return rxPersonRepository.list("name = :name", Parameters.with("name", "stef").map());
                            }).thenCompose(persons -> {
                                Assertions.assertEquals(1, persons.size());
                                Assertions.assertEquals(person, persons.get(0));

                                return rxPersonRepository.list("name = :name", Parameters.with("name", "stef"));
                            }).thenCompose(persons -> {
                                Assertions.assertEquals(1, persons.size());
                                Assertions.assertEquals(person, persons.get(0));

                                return rxPersonRepository.find("name", "stef").list();
                            }).thenCompose(persons -> {
                                Assertions.assertEquals(1, persons.size());
                                Assertions.assertEquals(person, persons.get(0));

                                return toList(rxPersonRepository.find("name = ?1", "stef").stream());
                            }).thenCompose(persons -> {
                                Assertions.assertEquals(1, persons.size());
                                Assertions.assertEquals(person, persons.get(0));

                                return toList(rxPersonRepository.stream("name = ?1", "stef"));
                            }).thenCompose(persons -> {
                                Assertions.assertEquals(1, persons.size());
                                Assertions.assertEquals(person, persons.get(0));

                                return toList(rxPersonRepository.find("name = :name", Parameters.with("name", "stef").map())
                                        .stream());
                            }).thenCompose(persons -> {
                                Assertions.assertEquals(1, persons.size());
                                Assertions.assertEquals(person, persons.get(0));

                                return toList(
                                        rxPersonRepository.find("name = :name", Parameters.with("name", "stef")).stream());
                            }).thenCompose(persons -> {
                                Assertions.assertEquals(1, persons.size());
                                Assertions.assertEquals(person, persons.get(0));

                                return toList(rxPersonRepository.stream("name = :name", Parameters.with("name", "stef").map()));
                            }).thenCompose(persons -> {
                                Assertions.assertEquals(1, persons.size());
                                Assertions.assertEquals(person, persons.get(0));

                                return toList(rxPersonRepository.stream("name = :name", Parameters.with("name", "stef")));
                            }).thenCompose(persons -> {
                                Assertions.assertEquals(1, persons.size());
                                Assertions.assertEquals(person, persons.get(0));

                                return toList(rxPersonRepository.find("name", "stef").stream());
                            }).thenCompose(persons -> {
                                Assertions.assertEquals(1, persons.size());
                                Assertions.assertEquals(person, persons.get(0));

                                return rxPersonRepository.find("name", "stef").firstResult();
                            }).thenCompose(p2 -> {
                                Assertions.assertEquals(person, p2);

                                return rxPersonRepository.find("name", "stef").singleResult();
                            }).thenCompose(p2 -> {
                                Assertions.assertEquals(person, p2);

                                return rxPersonRepository.findById(person.id);
                            }).thenCompose(byId -> {
                                Assertions.assertEquals(person, byId);

                                return person.delete();
                            })
                            .thenCompose(v -> rxPersonRepository.count())
                            .thenCompose(count -> {
                                Assertions.assertEquals(0, (long) count);

                                // Difference with JPA: no cascade
                                return rxDogRepository.deleteAll();
                            }).thenCompose(count -> {
                                Assertions.assertEquals(1, (long) count);

                                return makeSavedRxPersonRepository();
                            });
                }).thenCompose(newPerson -> {
                    return rxPersonRepository.count()
                            .thenCompose(count -> {
                                Assertions.assertEquals(1, (long) count);

                                return rxPersonRepository.delete("name = ?1", "emmanuel");
                            }).thenCompose(count -> {
                                Assertions.assertEquals(0, (long) count);

                                // FIXME: translate to owner_id
                                return rxDogRepository.delete("owner_id = ?1", newPerson.id);
                            }).thenCompose(count -> {
                                Assertions.assertEquals(1, (long) count);

                                return rxPersonRepository.delete("name", "stef");
                            }).thenCompose(count -> {
                                Assertions.assertEquals(1, (long) count);

                                return makeSavedRxPersonRepository();
                            });
                }).thenCompose(newPerson -> {
                    // FIXME: translate to owner_id
                    return rxDogRepository.delete("owner_id = :owner", Parameters.with("owner", newPerson.id).map())
                            .thenCompose(count -> {
                                Assertions.assertEquals(1, (long) count);

                                return rxPersonRepository.delete("name", "stef");
                            }).thenCompose(count -> {
                                Assertions.assertEquals(1, (long) count);

                                return makeSavedRxPersonRepository();
                            });
                }).thenCompose(newPerson -> {
                    // FIXME: translate to owner_id
                    return rxDogRepository.delete("owner_id = :owner", Parameters.with("owner", newPerson.id))
                            .thenCompose(count -> {
                                Assertions.assertEquals(1, (long) count);

                                return rxPersonRepository.delete("name", "stef");
                            }).thenCompose(count -> {
                                Assertions.assertEquals(1, (long) count);

                                return rxPersonRepository.deleteAll();
                            });
                }).thenCompose(count -> {
                    Assertions.assertEquals(0, (long) count);

                    return makeSavedRxPersonRepository();
                }).thenCompose(newPerson -> {
                    return rxDogRepository.deleteAll();
                }).thenCompose(count -> {
                    Assertions.assertEquals(1, (long) count);

                    return rxPersonRepository.deleteAll();
                }).thenCompose(count -> {
                    Assertions.assertEquals(1, (long) count);

                    return testPersistRepository(PersistTest.Iterable);
                }).thenCompose(v -> testPersistRepository(PersistTest.Stream))
                .thenCompose(v -> testPersistRepository(PersistTest.Variadic))
                .thenCompose(v -> rxPersonRepository.deleteAll())
                .thenCompose(c -> {
                    Assertions.assertEquals(6, c);

                    return testSortingRepository();
                }).thenCompose(v -> {
                    CompletionStage<Void> chain = CompletableFuture.completedFuture(null);
                    // paging
                    for (int i = 0; i < 7; i++) {
                        int finalI = i;
                        chain = chain
                                .thenCompose(v2 -> makeSavedRxPersonRepository(String.valueOf(finalI)).thenApply(p -> null));
                    }
                    return chain;
                }).thenCompose(v -> testPagingRepository(rxPersonRepository.findAll()))
                .thenCompose(v -> testPagingRepository(rxPersonRepository.find("ORDER BY name")))
                .thenCompose(v -> rxPersonRepository.findAll().singleResult().handle((v2, x) -> x))
                .thenCompose(x -> {
                    Assertions.assertTrue(x instanceof CompletionException);
                    Assertions.assertTrue(x.getCause() instanceof NonUniqueResultException);

                    return rxPersonRepository.findAll().firstResult();
                }).thenCompose(p -> {
                    Assertions.assertNotNull(p);

                    return rxPersonRepository.deleteAll();
                }).thenApply(count -> {
                    Assertions.assertEquals(7, count);

                    return "OK";
                });
    }

    private CompletionStage<Void> testPagingRepository(PanacheRxQuery<RxPerson> query) {
        // ints
        return query.page(0, 3).list()
                .thenCompose(persons -> {
                    Assertions.assertEquals(3, persons.size());
                    Assertions.assertEquals("stef0", persons.get(0).name);
                    Assertions.assertEquals("stef1", persons.get(1).name);
                    Assertions.assertEquals("stef2", persons.get(2).name);

                    return query.page(1, 3).list();
                }).thenCompose(persons -> {
                    Assertions.assertEquals(3, persons.size());
                    Assertions.assertEquals("stef3", persons.get(0).name);
                    Assertions.assertEquals("stef4", persons.get(1).name);
                    Assertions.assertEquals("stef5", persons.get(2).name);

                    return query.page(2, 3).list();
                }).thenCompose(persons -> {
                    Assertions.assertEquals(1, persons.size());
                    Assertions.assertEquals("stef6", persons.get(0).name);

                    return query.page(2, 4).list();
                }).thenCompose(persons -> {
                    Assertions.assertEquals(0, persons.size());

                    // page
                    Page page = new Page(3);
                    return query.page(page).list();
                }).thenCompose(persons -> {
                    Assertions.assertEquals(3, persons.size());
                    Assertions.assertEquals("stef0", persons.get(0).name);
                    Assertions.assertEquals("stef1", persons.get(1).name);
                    Assertions.assertEquals("stef2", persons.get(2).name);

                    Page page = new Page(3).next();
                    return query.page(page).list();
                }).thenCompose(persons -> {
                    Assertions.assertEquals(3, persons.size());
                    Assertions.assertEquals("stef3", persons.get(0).name);
                    Assertions.assertEquals("stef4", persons.get(1).name);
                    Assertions.assertEquals("stef5", persons.get(2).name);

                    Page page = new Page(3).next().next();
                    return query.page(page).list();
                }).thenCompose(persons -> {
                    Assertions.assertEquals(1, persons.size());
                    Assertions.assertEquals("stef6", persons.get(0).name);

                    Page page = new Page(3).next().next().next();
                    return query.page(page).list();
                }).thenCompose(persons -> {
                    Assertions.assertEquals(0, persons.size());

                    // query paging
                    Page page = new Page(3);
                    return query.page(page).list();
                }).thenCompose(persons -> {
                    Assertions.assertEquals(3, persons.size());
                    Assertions.assertEquals("stef0", persons.get(0).name);
                    Assertions.assertEquals("stef1", persons.get(1).name);
                    Assertions.assertEquals("stef2", persons.get(2).name);
                    Assertions.assertFalse(query.hasPreviousPage());

                    return query.hasNextPage();
                }).thenCompose(hasNextPage -> {
                    Assertions.assertTrue(hasNextPage);

                    return query.nextPage().list();
                }).thenCompose(persons -> {
                    Assertions.assertEquals(1, query.page().index);
                    Assertions.assertEquals(3, query.page().size);
                    Assertions.assertEquals(3, persons.size());
                    Assertions.assertEquals("stef3", persons.get(0).name);
                    Assertions.assertEquals("stef4", persons.get(1).name);
                    Assertions.assertEquals("stef5", persons.get(2).name);
                    Assertions.assertTrue(query.hasPreviousPage());

                    return query.hasNextPage();
                }).thenCompose(hasNextPage -> {
                    Assertions.assertTrue(hasNextPage);

                    return query.nextPage().list();
                }).thenCompose(persons -> {
                    Assertions.assertEquals(1, persons.size());
                    Assertions.assertEquals("stef6", persons.get(0).name);
                    Assertions.assertTrue(query.hasPreviousPage());

                    return query.hasNextPage();
                }).thenCompose(hasNextPage -> {
                    Assertions.assertFalse(hasNextPage);

                    return query.nextPage().list();
                }).thenCompose(persons -> {
                    Assertions.assertEquals(0, persons.size());

                    return query.count();
                }).thenCompose(count -> {
                    Assertions.assertEquals(7, count);

                    return query.pageCount();
                }).thenApply(pageCount -> {
                    Assertions.assertEquals(3, pageCount);

                    return null;
                });
    }

    private CompletionStage<Void> testSortingRepository() {
        RxPerson person1 = new RxPerson();
        person1.name = "stef";
        person1.status = Status.LIVING;
        return person1.save()
                .thenCompose(p1 -> {
                    RxPerson person2 = new RxPerson();
                    person2.name = "stef";
                    person2.status = Status.DECEASED;
                    return person2.save()
                            .thenCompose(p2 -> {
                                RxPerson person3 = new RxPerson();
                                person3.name = "emmanuel";
                                person3.status = Status.LIVING;
                                return person3.save();
                            }).thenCompose(p3 -> {
                                Sort sort1 = Sort.by("name", "status");
                                List<RxPerson> order1 = Arrays.asList(p3, person1, person2);
                                Sort sort2 = Sort.descending("name", "status");
                                List<RxPerson> order2 = Arrays.asList(person2, person1);

                                return rxPersonRepository.findAll(sort1).list()
                                        .thenCompose(list -> {
                                            Assertions.assertEquals(order1, list);

                                            return rxPersonRepository.listAll(sort1);
                                        }).thenCompose(list -> {
                                            Assertions.assertEquals(order1, list);

                                            return toList(rxPersonRepository.streamAll(sort1));
                                        }).thenCompose(list -> {
                                            Assertions.assertEquals(order1, list);

                                            return rxPersonRepository.find("name", sort2, "stef").list();
                                        }).thenCompose(list -> {
                                            Assertions.assertEquals(order2, list);

                                            return rxPersonRepository.list("name", sort2, "stef");
                                        }).thenCompose(list -> {
                                            Assertions.assertEquals(order2, list);

                                            return toList(rxPersonRepository.stream("name", sort2, "stef"));
                                        }).thenCompose(list -> {
                                            Assertions.assertEquals(order2, list);

                                            return rxPersonRepository
                                                    .find("name = :name", sort2, Parameters.with("name", "stef").map()).list();
                                        }).thenCompose(list -> {
                                            Assertions.assertEquals(order2, list);

                                            return rxPersonRepository.list("name = :name", sort2,
                                                    Parameters.with("name", "stef").map());
                                        }).thenCompose(list -> {
                                            Assertions.assertEquals(order2, list);

                                            return toList(rxPersonRepository.stream("name = :name", sort2,
                                                    Parameters.with("name", "stef").map()));
                                        }).thenCompose(list -> {
                                            Assertions.assertEquals(order2, list);

                                            return rxPersonRepository
                                                    .find("name = :name", sort2, Parameters.with("name", "stef")).list();
                                        }).thenCompose(list -> {
                                            Assertions.assertEquals(order2, list);

                                            return rxPersonRepository.list("name = :name", sort2,
                                                    Parameters.with("name", "stef"));
                                        }).thenCompose(list -> {
                                            Assertions.assertEquals(order2, list);

                                            return toList(rxPersonRepository.stream("name = :name", sort2,
                                                    Parameters.with("name", "stef")));
                                        }).thenCompose(list -> {
                                            Assertions.assertEquals(order2, list);

                                            return rxPersonRepository.deleteAll();
                                        }).thenApply(count -> {
                                            Assertions.assertEquals(3, count);

                                            return null;
                                        });
                            });
                });
    }

    private CompletionStage<Void> testPersistRepository(PersistTest persistTest) {
        RxPerson person1 = new RxPerson();
        person1.name = "stef1";
        RxPerson person2 = new RxPerson();
        person2.name = "stef2";
        Assertions.assertFalse(rxPersonRepository.isPersistent(person1));
        Assertions.assertFalse(rxPersonRepository.isPersistent(person2));
        CompletionStage<Void> persistOperation = null;
        switch (persistTest) {
            case Iterable:
                persistOperation = rxPersonRepository.save(Arrays.asList(person1, person2));
                break;
            case Stream:
                persistOperation = rxPersonRepository.save(Stream.of(person1, person2));
                break;
            case Variadic:
                persistOperation = rxPersonRepository.save(person1, person2);
                break;
        }
        return persistOperation
                .thenApply(v -> {
                    Assertions.assertTrue(rxPersonRepository.isPersistent(person1));
                    Assertions.assertTrue(rxPersonRepository.isPersistent(person2));

                    return v;
                });
    }

    private CompletionStage<? extends RxPerson> makeSavedRxPersonRepository(String suffix) {
        RxPerson person = new RxPerson();
        person.name = "stef" + suffix;
        person.status = Status.LIVING;

        try {
            return rxPersonRepository.save(person);
        } catch (Throwable t) {
            t.printStackTrace();
            throw new RuntimeException(t);
        }
    }

    private CompletionStage<? extends RxPerson> makeSavedRxPersonRepository() {
        return makeSavedRxPersonRepository("")
                .thenCompose(person -> {
                    System.err.println("Got Person ID " + person.id);
                    RxDog dog = new RxDog("octave", "dalmatian");
                    dog.owner = CompletableFuture.completedFuture(person);
                    person.dogs = ReactiveStreams.of(dog).buildRs();
                    return rxDogRepository.save(dog).thenApply(v -> person);
                });
    }
}
