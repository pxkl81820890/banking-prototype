# Questions to Ask During Kiro Training

Essential questions to maximize your Kiro training experience, organized by topic.

---

## 1. Basics & Getting Started

### Setup & Installation
- How do I install Kiro in my development environment?
- What are the system requirements?
- How do I configure Kiro for my IDE (VS Code, IntelliJ, etc.)?
- Can I use Kiro offline or does it require internet?
- How do I update Kiro to the latest version?

### Licensing & Credits
- How does the credit system work?
- How many credits do typical operations consume?
- What happens when I run out of credits?
- Are there ways to optimize credit usage?
- Can I see my credit usage history?

---

## 2. Spec-Driven Development

### Workflow Questions
- What's the difference between Requirements-First and Design-First workflows?
- When should I use each workflow?
- Can I switch between workflows mid-project?
- What if I want to skip the spec process and just write code?

### Spec Files
- What's the minimum required content for requirements.md?
- How detailed should design.md be?
- Can I manually edit spec files after Kiro generates them?
- What happens if I update a spec file after implementation?
- How do I version control spec files?

### Bugfix Workflow
- How is bugfix workflow different from feature workflow?
- What is "bug condition methodology"?
- When should I use bugfix workflow vs feature workflow?
- Can I convert a feature spec to a bugfix spec?

---

## 3. Steering Files & Standards

### Configuration
- Where should I put steering files? (workspace vs user-level)
- What's the hierarchy when Kiro reads steering files?
- Can I override organization-wide standards for my project?
- How do I share steering files across multiple repositories?

### Content
- What should go in steering files vs spec files?
- How detailed should steering files be?
- Can I reference external documentation in steering files?
- How do I enforce that Kiro follows my steering files?

### Best Practices
- What are examples of good steering files?
- What common mistakes should I avoid?
- How often should I update steering files?
- Can steering files reference other steering files?

---

## 4. Code Generation

### Quality & Control
- How do I ensure Kiro generates high-quality code?
- Can I review code before Kiro commits it?
- How do I handle code that Kiro generates incorrectly?
- Can I provide feedback to improve future generations?

### Customization
- How do I make Kiro follow my team's coding style?
- Can Kiro use our existing code as examples?
- How do I prevent Kiro from using certain patterns?
- Can I teach Kiro about our custom frameworks/libraries?

### Incremental Updates
- How does Kiro handle updating existing code?
- Will Kiro overwrite my manual changes?
- How do I tell Kiro to only update specific parts?
- Can Kiro refactor existing code?

---

## 5. Testing

### Test Generation
- What types of tests can Kiro generate?
- How do I ensure good test coverage?
- Can Kiro generate property-based tests?
- How does Kiro handle mocking external dependencies?

### Test Quality
- How do I verify that generated tests are meaningful?
- Can Kiro update tests when code changes?
- How does Kiro handle integration tests?
- Can Kiro generate test data/fixtures?

---

## 6. Multi-Service & Team Collaboration

### Shared Configuration
- How do I share Kiro config across microservices?
- What's the best way to sync shared config in multi-repo setup?
- How do I handle service-specific overrides?
- Can different services use different Kiro versions?

### Team Workflow
- How do multiple developers use Kiro on the same project?
- How do we handle merge conflicts in spec files?
- Should spec files be in version control?
- How do we coordinate steering file updates across the team?

### Code Reviews
- How should we review Kiro-generated code?
- What should reviewers focus on?
- How do we document that code was AI-generated?
- Can Kiro help with code review process?

---

## 7. AWS-Specific Questions

### AWS Integration
- Can Kiro generate AWS SDK code?
- Does Kiro know AWS best practices?
- Can Kiro generate CloudFormation/Terraform templates?
- How does Kiro handle AWS service-specific patterns?

### AWS Services
- Can Kiro generate Lambda functions?
- Can Kiro create API Gateway configurations?
- Can Kiro generate DynamoDB access patterns?
- Can Kiro create Step Functions workflows?
- Can Kiro generate IAM policies?

### AWS Best Practices
- How do I ensure Kiro follows AWS Well-Architected Framework?
- Can Kiro help with AWS cost optimization?
- Can Kiro generate AWS security best practices?
- How does Kiro handle AWS service limits?

---

## 8. Advanced Features

### Hooks
- What are Kiro hooks and when should I use them?
- What events can trigger hooks?
- Can hooks run shell commands?
- How do I debug hooks that aren't working?
- Can hooks call external APIs?

### Powers (MCP)
- What are Kiro Powers?
- How do I install and use Powers?
- Can I create custom Powers?
- What Powers are available for AWS development?

### Context Management
- How does Kiro decide what files to read?
- How can I give Kiro more context?
- How do I prevent Kiro from reading certain files?
- What's the maximum context size?

---

## 9. Troubleshooting & Debugging

### Common Issues
- What do I do if Kiro generates incorrect code?
- How do I fix "out of credits" errors?
- What if Kiro doesn't follow my steering files?
- How do I handle Kiro timeouts?
- What if Kiro can't find my files?

### Debugging
- How do I see what Kiro is reading?
- Can I see Kiro's "thought process"?
- How do I report bugs or issues?
- Where can I find Kiro logs?

### Performance
- Why is Kiro slow sometimes?
- How can I make Kiro faster?
- Does file size affect Kiro's performance?
- Can I run Kiro operations in parallel?

---

## 10. Best Practices & Tips

### Productivity
- What are the most useful Kiro commands?
- What keyboard shortcuts should I know?
- How do I use Kiro most efficiently?
- What are common time-wasters to avoid?

### Quality
- How do I ensure consistent code quality?
- What should I always review in Kiro-generated code?
- How do I prevent technical debt from AI-generated code?
- What metrics should I track?

### Learning
- What resources are available for learning Kiro?
- Are there example projects I can study?
- Is there a Kiro community or forum?
- How do I stay updated on new features?

---

## 11. Integration with Existing Tools

### Version Control
- How does Kiro work with Git?
- Should I commit Kiro-generated files differently?
- How do I handle Kiro changes in pull requests?
- Can Kiro generate commit messages?

### CI/CD
- Can Kiro be used in CI/CD pipelines?
- How do I automate Kiro in GitHub Actions/GitLab CI?
- Can Kiro validate code before deployment?
- How do I handle Kiro credits in CI/CD?

### Project Management
- Can Kiro integrate with Jira/Linear?
- Can Kiro read requirements from external tools?
- Can Kiro update task status automatically?
- How do I link Kiro specs to tickets?

---

## 12. Security & Compliance

### Data Privacy
- What data does Kiro send to the cloud?
- Is my code stored anywhere?
- How is my data protected?
- Can I use Kiro with proprietary code?

### Compliance
- Is Kiro SOC2/HIPAA/PCI-DSS compliant?
- Can I audit Kiro's actions?
- How do I ensure Kiro doesn't generate insecure code?
- Can I restrict what Kiro can access?

---

## 13. Cost & ROI

### Pricing
- How much does Kiro cost per developer?
- Are there team/enterprise plans?
- What's included in each plan?
- Are there discounts for annual subscriptions?

### ROI
- How do I measure Kiro's ROI?
- What metrics should I track?
- How long until we see productivity gains?
- What's the typical time-to-value?

---

## 14. Migration & Adoption

### Getting Started
- How do I introduce Kiro to my existing project?
- Should I start with a new project or existing one?
- What's the learning curve?
- How long until developers are productive?

### Team Adoption
- How do I get buy-in from skeptical team members?
- What's the best rollout strategy?
- Should we do a pilot first?
- How do we handle resistance to AI tools?

### Migration
- Can Kiro help migrate from other AI tools?
- How do I convert existing documentation to Kiro specs?
- Can Kiro analyze existing code and generate specs?
- How do I migrate from manual development to spec-driven?

---

## 15. Future & Roadmap

### Upcoming Features
- What new features are coming?
- Is there a public roadmap?
- Can I request features?
- How often are new features released?

### Long-term
- What's Kiro's vision for the future?
- Will Kiro support more languages/frameworks?
- Are there plans for on-premise deployment?
- How will Kiro evolve with AI advancements?

---

## 16. Hands-On Practice Questions

### During Demos
- Can you show me how to [specific task]?
- What would you do if [specific scenario]?
- Can we try this with my actual code?
- What's the best way to handle [my use case]?

### Real-World Scenarios
- How would Kiro handle our [specific architecture]?
- Can Kiro work with [our specific framework]?
- How do we use Kiro for [our specific workflow]?
- Can you show an example with [our tech stack]?

---

## 17. Post-Training Support

### Resources
- What documentation is available?
- Are there video tutorials?
- Is there a Kiro community/forum?
- How do I get help after training?

### Ongoing Learning
- Are there advanced training sessions?
- Can I schedule follow-up sessions?
- Are there Kiro certifications?
- How do I become a Kiro expert?

---

## Priority Questions (Ask These First!)

If time is limited, prioritize these:

1. **How does the credit system work and how do I optimize usage?**
2. **What's the best workflow for my team's use case?**
3. **How do I share configuration across multiple services/repos?**
4. **How do I ensure Kiro follows our coding standards?**
5. **What's the best way to review and validate Kiro-generated code?**
6. **How do I handle Kiro-generated code in pull requests?**
7. **What are the most common mistakes to avoid?**
8. **How do I troubleshoot when Kiro doesn't do what I expect?**
9. **What AWS-specific features does Kiro have?**
10. **What support is available after training?**

---

## Questions Based on Your Current Setup

### For Your Banking Microservices Project

- How do I set up shared Kiro config for 5+ microservices?
- Can Kiro help with Spring Boot + Hexagonal Architecture?
- How do I use Kiro with our Gateway + Keycloak setup?
- Can Kiro generate OpenAPI/Swagger documentation?
- How do I use Kiro with React Micro Frontends?
- Can Kiro help with Module Federation configuration?
- How do I ensure consistency across all services?

---

## Tips for Asking Questions

1. **Be Specific** - Instead of "How does Kiro work?", ask "How does Kiro generate tests for Spring Boot controllers?"

2. **Use Real Examples** - Bring code from your actual project to discuss

3. **Ask "Why"** - Understand the reasoning behind best practices

4. **Ask for Alternatives** - "What are other ways to do this?"

5. **Ask for Demos** - "Can you show me how to do this?"

6. **Take Notes** - Write down answers and examples

7. **Ask for Resources** - "Where can I learn more about this?"

8. **Follow Up** - If something is unclear, ask for clarification

---

## After Training

Questions to ask yourself:
- What did I learn that I can apply immediately?
- What do I need to practice more?
- What questions do I still have?
- Who can I ask for help?
- What should I teach my team?

---

Good luck with your training! 🚀
