# Contributing to scala-test-metrics

First off, thanks for taking the time to contribute! 🎉👍

## The Zen of scala-test-metrics

```
Beautiful is better than ugly.
Explicit is better than implicit.
Simple is better than complex.
Complex is better than complicated.
Readability counts.
Special cases aren't special enough to break the rules.
Although practicality beats purity.
Errors should never pass silently.
Unless explicitly silenced.
In the face of ambiguity, refuse the temptation to guess.
There should be one-- and preferably only one --obvious way to do it.
Although that way may not be obvious at first unless you're Scala.
Now is better than never.
Although never is often better than *right* now.
If the implementation is hard to explain, it's a bad idea.
If the implementation is easy to explain, it may be a good idea.
Namespaces are one honking great idea -- let's do more of those!
```

(With apologies to Python's Zen)

## Project Structure

This project is a monorepo containing both the ScalaTest and JUnit listeners. They are versioned and published together, because we believe in the "go big or go home" philosophy.

## Versioning

We use semantic versioning, sort of. The major and minor versions are defined in the build YAML, while the patch version automatically increments with the build number. It's like a mullet: business in the front, party in the back.

## Pull Request Process

1. Ensure any install or build dependencies are removed before the end of the layer when doing a build.
2. Update the README.md with details of changes to the interface, this includes new environment variables, exposed ports, useful file locations and container parameters.
3. Increase the version numbers in any examples files and the README.md to the new version that this Pull Request would represent. The versioning scheme we use is SemVer, with a twist of automation.
4. You may merge the Pull Request in once you have the sign-off of two other developers, or if you don't have permission to do that, you may request the second reviewer to merge it for you.

## Code of Conduct

In the interest of fostering an open and welcoming environment, we as contributors and maintainers pledge to making participation in our project and our community a harassment-free experience for everyone, regardless of age, body size, disability, ethnicity, gender identity and expression, level of experience, nationality, personal appearance, race, religion, or sexual identity and orientation.

Unless you're a tabs-instead-of-spaces person. Then all bets are off. (Just kidding, we love all developers equally. Except Python developers. They know what they did.)

## Testing

We believe in test-driven development. In fact, we believe in it so much that we've created a tool to test our tests. It's very meta.

Remember: "Program testing can be used to show the presence of bugs, but never to show their absence!" - Edsger W. Dijkstra

## Code Review

All submissions, including submissions by project members, require review. We use GitHub pull requests for this purpose. Consult [GitHub Help](https://help.github.com/articles/about-pull-requests/) for more information on using pull requests.

During code review, if you see something that could be improved, don't just say "This could be better." Instead, submit a pull request to fix it. Be the change you want to see in the codebase.

## Documentation

Remember, code tells you how, comments tell you why. Please document your code as if the person who ends up maintaining it is a violent psychopath who knows where you live.

## And finally...

Remember, in software development, there are only two types of projects: those that are broken and those that are not finished yet. Let's strive to be the latter!

Happy coding! 🚀