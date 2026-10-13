"""Verify model delegation, bounded tools, review veto and urgent short circuit."""
import json
import unittest
from engine import ModelClient, respond

class SpecialistModel(ModelClient):
    def __init__(self, selected='activities', unsafe=False):
        self.selected=selected; self.unsafe=unsafe; self.calls=[]
    @property
    def ready(self): return True
    def chat(self,system,user,max_tokens=450):
        self.calls.append(system)
        if 'classify risk' in system: return json.dumps({'english':json.loads(user)['message'] if user.startswith('{') else user.split('Text: ')[-1], 'risk':'ordinary'})
        if 'Supervisor agent' in system: return json.dumps({'action':self.selected})
        if 'Activity agent' in system: return json.dumps({'activities':['memory','art'] if not self.unsafe else ['send_to_doctor']})
        if 'Review agent' in system: return json.dumps({'pass':not self.unsafe})
        return 'I would like to discuss worries about travel at my next visit.'

class AgentTests(unittest.TestCase):
    def test_supervisor_delegates_to_activity_agent_and_exposes_useful_tools(self):
        result=respond({'text':'I need a distraction'},SpecialistModel())
        self.assertEqual(result['action'],'activities')
        self.assertEqual([x['id'] for x in result['activity_plan']],['memory','art'])
        self.assertEqual([x['agent'] for x in result['agent_trace']],['Language','Safety','Supervisor','Activity'])
        self.assertEqual(result['mode'],'live')
    def test_unknown_activity_tool_falls_back_to_catalogue(self):
        result=respond({'text':'A quiet activity please','action':'activities','energy':'low'},SpecialistModel(unsafe=True))
        self.assertEqual(result['mode'],'demo')
        self.assertEqual([x['id'] for x in result['activity_plan']],['sound','peers'])
    def test_low_energy_plan_does_not_accept_medium_energy_model_choices(self):
        result=respond({'text':'A gentle plan please','action':'activities','energy':'low'},SpecialistModel())
        self.assertEqual([x['id'] for x in result['activity_plan']],['sound','peers'])
    def test_urgent_request_never_calls_any_model(self):
        model=SpecialistModel()
        result=respond({'text':'I have a fever','action':'activities'},model)
        self.assertEqual(result['route'],'urgent_medical'); self.assertEqual(model.calls,[])
    def test_brief_is_editable_output_and_reviewed_before_display(self):
        result=respond({'text':'Draft my concerns about travel','action':'brief'},SpecialistModel('brief'))
        self.assertEqual(result['mode'],'live')
        self.assertEqual(result['visit_brief'],result['text'])
        self.assertIn('Visit preparation',[x['agent'] for x in result['agent_trace']])
        self.assertEqual(result['agent_trace'][-1]['outcome'],'approved')
        veto=respond({'text':'Draft my concerns about travel','action':'brief'},SpecialistModel('brief',True))
        self.assertEqual(veto['mode'],'fallback')
        self.assertEqual(veto['agent_trace'][-1]['outcome'],'vetoed')


class CatalogueParityTests(unittest.TestCase):
    """Every activity the Android app lists must exist in the backend catalogue, and vice versa."""
    def test_android_entries_match_backend_catalogue(self):
        import re, pathlib
        from agents import ACTIVITIES
        src = (pathlib.Path(__file__).parent.parent / 'android/app/src/main/java/org/sahay/app/WellbeingUi.kt').read_text()
        listed = set(re.findall(r'ActivityEntry\("(\w+)"', src))
        routed = set(re.findall(r'^\s+"(\w+)" -> \w+Activity\(', src, re.M))
        self.assertEqual(listed, set(ACTIVITIES))
        self.assertEqual(listed, routed)
