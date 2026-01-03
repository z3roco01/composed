package z3roco01.composed;

import z3roco01.composed.annotation.Comment;
import z3roco01.composed.annotation.ConfigProperty;

public class TestConfig {
    @Comment(comment = "this is a comment hello")
    @ConfigProperty
    public Integer test = 99;

    @ConfigProperty
    public boolean hello = false;

    @ConfigProperty
    public float oh = 1.325f;

    @ConfigProperty
    public String yes = "what the flart";
}
