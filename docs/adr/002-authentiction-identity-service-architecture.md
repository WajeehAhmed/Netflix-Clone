Status: Accepted
Context: We need an Identity Service for a Netflix-clone microservices architecture that handles user registration, role-based access, and secure authentication via JWT.

Decision:

Identity Storage: We will use a relational PostgreSQL database with a normalized Many-to-Many relationship between Users and Roles.

Schema Management: We will use Liquibase for version-controlled database migrations instead of Hibernate's auto-generation.

Security: We will implement JWT for stateless authentication. Secrets will be managed at runtime using HashiCorp Vault’s Kubernetes authentication method.

Performance: We will use Entity Graphs (@EntityGraph) on UserRepository to avoid the N+1 problem when fetching user roles.

Consequences (Trade-offs):

Pro: Highly normalized schema allows for easy extension (e.g., adding granular permissions or subscription tiers later).

Pro: Liquibase provides a clear audit trail and reliable deployments across environments.

Con: Slightly more complex setup compared to simple @ElementCollection or Hibernate ddl-auto: update.

Con: Requires maintaining a db.changelog-master.yaml file for every database schema change.