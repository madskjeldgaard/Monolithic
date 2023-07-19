/*

Palindrome

TODO: Make an .asPalindrome method for all patterns

*/

Ppalindrome : ListPattern {
	*new { |list, repeats=1, offset=0|

		^Pseq.new(list ++ list.reverse, repeats, offset)
	}
}
