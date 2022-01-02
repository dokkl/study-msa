package com.hoon.msa.core.review;

import com.hoon.api.core.product.Product;
import com.hoon.api.core.review.Review;
import com.hoon.api.event.Event;
import com.hoon.msa.core.review.persistence.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.http.HttpStatus;
import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.hoon.api.event.Event.Type.CREATE;
import static com.hoon.api.event.Event.Type.DELETE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static reactor.core.publisher.Mono.just;

@RunWith(SpringRunner.class)
@SpringBootTest(
		webEnvironment=RANDOM_PORT,
		properties = {"logging.level.com.hoon=DEBUG", "spring.datasource.url=jdbc:h2:mem:review-db"})
class ReviewServiceApplicationTests {
	@Autowired
	private WebTestClient client;

	@Autowired
	private ReviewRepository repository;

	@Autowired
	private Sink channels;

	private AbstractMessageChannel input = null;

	@BeforeEach
	public void setupDb() {
		input = (AbstractMessageChannel) channels.input();
		repository.deleteAll();
	}

	@Test
	public void getReviewsByProductId() {

		int productId = 1;

		assertEquals(0, repository.findByProductId(productId).size());

//		postAndVerifyReview(productId, 1, OK);
//		postAndVerifyReview(productId, 2, OK);
//		postAndVerifyReview(productId, 3, OK);
		sendCreateReviewEvent(productId, 1);
		sendCreateReviewEvent(productId, 2);
		sendCreateReviewEvent(productId, 3);

		assertEquals(3, repository.findByProductId(productId).size());

		getAndVerifyReviewsByProductId(productId, OK)
				.jsonPath("$.length()").isEqualTo(3)
				.jsonPath("$[2].productId").isEqualTo(productId)
				.jsonPath("$[2].reviewId").isEqualTo(3);
	}

//	@Test
//	public void duplicateError() {
//
//		int productId = 1;
//		int reviewId = 1;
//
//		assertEquals(0, repository.count());
//
//		postAndVerifyReview(productId, reviewId, OK)
//				.jsonPath("$.productId").isEqualTo(productId)
//				.jsonPath("$.reviewId").isEqualTo(reviewId);
//
//		assertEquals(1, repository.count());
//
//		postAndVerifyReview(productId, reviewId, UNPROCESSABLE_ENTITY)
//				.jsonPath("$.path").isEqualTo("/review")
//				.jsonPath("$.message").isEqualTo("Duplicate key, Product Id: 1, Review Id:1");
//
//		assertEquals(1, repository.count());
//	}

	@Test
	public void deleteReviews() {

		int productId = 1;
		int reviewId = 1;

//		postAndVerifyReview(productId, recommendationId, OK);
		sendCreateReviewEvent(productId, reviewId);
		assertEquals(1, repository.findByProductId(productId).size());

//		deleteAndVerifyReviewsByProductId(productId, OK);
		sendDeleteReviewEvent(productId);
		assertEquals(0, repository.findByProductId(productId).size());

		sendDeleteReviewEvent(productId);
//		deleteAndVerifyReviewsByProductId(productId, OK);
	}

	@Test
	public void getReviewsMissingParameter() {

		getAndVerifyReviewsByProductId("", BAD_REQUEST)
				.jsonPath("$.path").isEqualTo("/review")
				.jsonPath("$.message").isEqualTo("Required int parameter 'productId' is not present");
	}

	@Test
	public void getReviewsInvalidParameter() {

		getAndVerifyReviewsByProductId("?productId=no-integer", BAD_REQUEST)
				.jsonPath("$.path").isEqualTo("/review")
				.jsonPath("$.message").isEqualTo("Type mismatch.");
	}

	@Test
	public void getReviewsNotFound() {

		getAndVerifyReviewsByProductId("?productId=213", OK)
				.jsonPath("$.length()").isEqualTo(0);
	}

	@Test
	public void getReviewsInvalidParameterNegativeValue() {

		int productIdInvalid = -1;

		getAndVerifyReviewsByProductId("?productId=" + productIdInvalid, UNPROCESSABLE_ENTITY)
				.jsonPath("$.path").isEqualTo("/review")
				.jsonPath("$.message").isEqualTo("Invalid productId: " + productIdInvalid);
	}

	private WebTestClient.BodyContentSpec getAndVerifyReviewsByProductId(int productId, HttpStatus expectedStatus) {
		return getAndVerifyReviewsByProductId("?productId=" + productId, expectedStatus);
	}

	private WebTestClient.BodyContentSpec getAndVerifyReviewsByProductId(String productIdQuery, HttpStatus expectedStatus) {
		return client.get()
				.uri("/review" + productIdQuery)
				.accept(APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(expectedStatus)
				.expectHeader().contentType(APPLICATION_JSON)
				.expectBody();
	}

	private WebTestClient.BodyContentSpec postAndVerifyReview(int productId, int reviewId, HttpStatus expectedStatus) {
		Review review = new Review(productId, reviewId, "Author " + reviewId, "Subject " + reviewId, "Content " + reviewId, "SA");
		return client.post()
				.uri("/review")
				.body(just(review), Review.class)
				.accept(APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(expectedStatus)
				.expectHeader().contentType(APPLICATION_JSON)
				.expectBody();
	}

	private WebTestClient.BodyContentSpec deleteAndVerifyReviewsByProductId(int productId, HttpStatus expectedStatus) {
		return client.delete()
				.uri("/review?productId=" + productId)
				.accept(APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(expectedStatus)
				.expectBody();
	}

	private void sendCreateReviewEvent(int productId, int reviewId) {
		Review review = new Review(productId, reviewId, "Author " + reviewId, "Subject " + reviewId, "Content " + reviewId, "SA");
		Event<Integer, Product> event = new Event(CREATE, productId, review);
		input.send(new GenericMessage<>(event));
	}

	private void sendDeleteReviewEvent(int productId) {
		Event<Integer, Product> event = new Event(DELETE, productId, null);
		input.send(new GenericMessage<>(event));
	}

	@Test
	void contextLoads() {
	}

}
