Title: ADR-001: Use JSON Web Tokens (JWT) for Authentication

Status: Accepted

Context:
Our system consists of multiple microservices (Identity, Metadata, Gateway). Using traditional server-side sessions would require sticky load balancing and a shared session store (like a central Redis cluster), increasing architectural complexity and introducing a single point of failure.

Decision:
We will use stateless JWTs issued by the Identity Service. These tokens will be signed and verified by the API Gateway and downstream services.

Consequences:

Pros: Highly scalable, no session synchronization required across nodes, reduces database hits for session validation.

Cons: Token revocation is harder (requires a blacklist in Redis), and if the secret key is compromised, all tokens are invalid.