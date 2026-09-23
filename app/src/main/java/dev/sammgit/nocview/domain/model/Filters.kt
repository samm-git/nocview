package dev.sammgit.nocview.domain.model

enum class HostFilter {
    ALL,
    UP,
    DOWN,
    UNREACHABLE,
}

enum class ServiceFilter {
    ALL,
    OK,
    WARNING,
    CRITICAL,
    UNKNOWN,
}
