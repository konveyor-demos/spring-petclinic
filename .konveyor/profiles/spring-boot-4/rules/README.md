# Rules for the Spring Boot 3 to 4 demo

The in-tree profile `../profile.yaml` combines two kinds of rules:

## `upstream/` (copied, do not edit)

Copied unchanged from [konveyor/rulesets](https://github.com/konveyor/rulesets) (Apache-2.0):

| Folder | Source path |
|---|---|
| `upstream/spring-boot-4` | `stable/java/spring-boot-4` |
| `upstream/spring-framework-7` | `stable/java/spring-framework-7` |

- Commit: `cd2584070d01645113ee11975f5a0fb8160381db` (2026-11-04)
- The `tests/` folders were left out.
- Why copied: these rulesets were added on 2026-09-24 and are not part of a tagged
  ruleset release yet. The VS Code extension v0.8.x bundles rulesets `v0.11.0-beta.1`
  (June 2026), which does not contain them.
- To update: copy the two folders again from a newer commit and update the commit above.

## `custom/` (our own additions)

`custom/spring-boot-4-test-slices` covers test slice annotations that Spring Boot 4 moved to
technology-specific test modules. The upstream `spring-boot-4` ruleset does not cover them,
so the build breaks after upgrading the parent POM:

| Spring Boot 3 | Spring Boot 4 | Test starter |
|---|---|---|
| `o.s.b.test.autoconfigure.web.servlet.WebMvcTest` | `o.s.b.webmvc.test.autoconfigure.WebMvcTest` | `spring-boot-starter-webmvc-test` |
| `o.s.b.test.autoconfigure.web.servlet.AutoConfigureMockMvc` | `o.s.b.webmvc.test.autoconfigure.AutoConfigureMockMvc` | `spring-boot-starter-webmvc-test` |
| `o.s.b.test.autoconfigure.orm.jpa.DataJpaTest` | `o.s.b.data.jpa.test.autoconfigure.DataJpaTest` | `spring-boot-starter-data-jpa-test` |
| `o.s.b.test.autoconfigure.jdbc.AutoConfigureTestDatabase` | `o.s.b.jdbc.test.autoconfigure.AutoConfigureTestDatabase` | via `spring-boot-starter-data-jpa-test` |

New packages verified against the Spring Boot API docs (4.1.x).
