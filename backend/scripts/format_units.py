import json
import re
import sys
import os

def format_json_file(file_path):
    if not os.path.exists(file_path):
        print(f"File not found: {file_path}")
        return

    with open(file_path, 'r', encoding='utf-8') as f:
        try:
            data = json.load(f)
        except json.JSONDecodeError as e:
            print(f"Error parsing JSON: {e}")
            return

    # First, get a clean pretty-printed version with standard 4-space indentation
    formatted_content = json.dumps(data, indent=4, ensure_ascii=False)

    # Regex to find arrays of exactly 3 numbers and collapse them
    # It looks for:
    # [
    #     number,
    #     number,
    #     number
    # ]
    # and replaces it with: [number, number, number]
    
    # The regex matches:
    # '[' 
    # '\n' and any whitespace 
    # group 1 (a number) 
    # ',' '\n' and any whitespace
    # group 2 (a number)
    # ',' '\n' and any whitespace
    # group 3 (a number)
    # '\n' and any whitespace
    # ']'
    
    # Regex for a number (int or float, optional negative sign)
    num_pattern = r'-?\d+(?:\.\d+)?'
    
    pattern = re.compile(
        r'\[\n\s*(' + num_pattern + r'),\n\s*(' + num_pattern + r'),\n\s*(' + num_pattern + r')\n\s*\]'
    )
    collapsed = pattern.sub(r'[\1, \2, \3]', formatted_content)

    # Collapse "traits" arrays 
    # This matches '"traits": [' followed by lines of "TraitName" and ends with ']'
    traits_pattern = re.compile(r'("traits":\s*\[)([^\]]+)(\])', re.MULTILINE | re.DOTALL)
    
    def collapse_traits(match):
        prefix = match.group(1)
        content = match.group(2)
        suffix = match.group(3)
        # Remove newlines and extra spaces within the content, keep comma-space separation
        # First split by newline, then strip each line, then filter out empty lines
        lines = [line.strip() for line in content.split('\n') if line.strip()]
        new_content = ' '.join(lines)
        # Ensure space after commas if they don't have one
        new_content = re.sub(r',(?!\s)', ', ', new_content)
        return f'{prefix}{new_content}{suffix}'

    collapsed = traits_pattern.sub(collapse_traits, collapsed)

    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(collapsed)
        # Ensure a trailing newline
        if not collapsed.endswith('\n'):
            f.write('\n')
    
    print(f"Successfully formatted: {file_path}")

if __name__ == "__main__":
    if len(sys.argv) > 1:
        for arg in sys.argv[1:]:
            format_json_file(arg)
    else:
        # If no arguments provided, try default path
        # Assuming run from project root, but let's be flexible
        default_path = "backend/src/main/resources/data/units_onepiece.json"
        if os.path.exists(default_path):
            format_json_file(default_path)
        else:
            print("Usage: python format_units.py <path_to_json_file>")
